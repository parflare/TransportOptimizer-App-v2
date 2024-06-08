package ua.parflare.transportoptimizerapp.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.util.*;

@Component
public class TransportOptimizerUtil {

    private static final int GENERATIONS = 300;
    private static final double MUTATION_RATE = 0.08;
    private static final int MAX_INCREMENT_MINUTES = 5;
    private static final int MAX_DECREMENT_MINUTES = 5;
    private static final int POPULATION_SIZE = 30;
    private DataStructure original;
    private ArrayList<DataStructure> population = new ArrayList<>();
    private DataStructure maxScoreData;


    public TransportOptimizerUtil() {
        original = null;
        maxScoreData = null;
    }

    private static ArrayList<Date> getDateArrayList(ArrayList<Date> timeList) {
        ArrayList<Date> filteredTimes = new ArrayList<>();

        for (int i = 0; i < timeList.size(); i++) {
            Date currentTime = timeList.get(i);
            if (i + 1 < timeList.size()) {
                Date nextTime = timeList.get(i + 1);
                long interval = (nextTime.getTime() - currentTime.getTime()) / (60 * 1000); // Interval in minutes
                if (interval < 1) {
                    if (!filteredTimes.contains(currentTime)) {
                        filteredTimes.add(currentTime);
                    }
                    filteredTimes.add(nextTime);
                }
            }
        }
        return filteredTimes;
    }

    public void initializeStationData(ArrayList<StationData> initialPopulation) {
        original = new DataStructure(initialPopulation);
        maxScoreData = new DataStructure(original);
        population.add(new DataStructure(original.getData()));
    }

    public ArrayList<StationData> optimizeSchedule(ArrayList<StationData> initialPopulation) {
        initializeStationData(initialPopulation);

        original = new DataStructure(initialPopulation);
        maxScoreData = new DataStructure(original);
        population.add(new DataStructure(original.getData()));
        System.out.println();
        int currentGeneration = 0;
        int attempt = 0;

        int maxFitness = initMaxFitness(original.getData());
        int origFitness = evaluateFitness(original.getData());
        ArrayList<DataStructure> newPopulation;
        int tmpFitness = 0;
        while (currentGeneration < GENERATIONS && tmpFitness != maxFitness) {

            newPopulation = new ArrayList<>(GENERATIONS);
            while (newPopulation.size() < POPULATION_SIZE && tmpFitness != maxFitness) {
                attempt++;
                DataStructure parent1 = selectParent();
                DataStructure parent2 = selectParent();
                DataStructure offspring = crossover(parent1, parent2);
                if (Math.random() < MUTATION_RATE) {
                    mutate(offspring);
                }
                offspring.reevaData();

                if (isWithinAllowedDeviation(offspring)) {
                    newPopulation.add(offspring);
                }
                tmpFitness = offspring.getFitness();
                System.out.printf("\rcurrentFitness: %d, maxFitness: %d->%d/%d, iter: %d, popul: %d, attempt: %d", tmpFitness, origFitness, maxScoreData.getFitness(), maxFitness, currentGeneration, newPopulation.size(), attempt);
            }
            attempt = 0;
            updateMaxScore(newPopulation);
            System.out.printf("\rcurrentFitness: %d, maxFitness: %d->%d/%d, iter: %d", tmpFitness, origFitness, maxScoreData.getFitness(), maxFitness, ++currentGeneration);

            population = new ArrayList<>(newPopulation);

        }

        System.out.println();

        return maxScoreData.getData();
    }

    private boolean isWithinAllowedDeviation(DataStructure offspring) {
        ArrayList<StationData> originalData = original.getData();
        ArrayList<StationData> offspringData = offspring.getData();

        for (int i = 0; i < originalData.size(); i++) {
            ArrayList<Date> originalTimes = originalData.get(i).getRouteTime();
            ArrayList<Date> offspringTimes = offspringData.get(i).getRouteTime();

            for (int j = 0; j < originalTimes.size(); j++) {
                long originalTime = originalTimes.get(j).getTime();
                long offspringTime = offspringTimes.get(j).getTime();
                long timeDifference = (offspringTime - originalTime) / (60 * 1000); // Difference in minutes

                if (timeDifference > MAX_INCREMENT_MINUTES || timeDifference < -MAX_DECREMENT_MINUTES) {
                    return false;
                }
            }
        }
        return true;
    }

    public void updateMaxScore(ArrayList<DataStructure> dataStructure) {
        for (DataStructure data : dataStructure) {
            data.reevaData();
            if (data.getFitness() > maxScoreData.getFitness()) {
                maxScoreData = new DataStructure(data);
            }
        }

    }

    private int initMaxFitness(ArrayList<StationData> data) {
        int genSize = 0;
        for (StationData datum : data) {
            genSize += datum.getRouteTime().size();
        }
        return genSize;
    }

    private int evaluateFitness(ArrayList<StationData> data) {
        int genConf = countConflicts(data);
        int genSize = 0;
        for (StationData stationData : data) {
            int conflicts = countMiniConflicts(stationData.getRouteTime());
            stationData.setMiniFitness(stationData.getRouteTime().size() - conflicts);
            genSize += stationData.getRouteTime().size();
        }

        return genSize - genConf; // Максимальна кількість балів - кількість конфліктів
    }

    private Map<String, Map<String, ArrayList<Date>>> loadTimes(ArrayList<StationData> stationDataList) {
        Map<String, Map<String, ArrayList<Date>>> times = new HashMap<>();
        // Collect times for each station and working days
        for (StationData data : stationDataList) {
            times.computeIfAbsent(data.getStationName(), k -> new HashMap<>())
                    .computeIfAbsent(data.getRouteWorkingDays(), k -> new ArrayList<>())
                    .addAll(data.getRouteTime());
        }
        return times;
    }

    private int countConflicts(ArrayList<StationData> stationDataList) {
        int conflicts = 0;
        Map<String, Map<String, ArrayList<Date>>> times = loadTimes(stationDataList);

        Map<String, Set<String>> routesForStations = new HashMap<>();

        // Збираємо маршрути для кожної станції
        for (StationData data : stationDataList) {
            // Переконуємося, що в мапі є запис для назви станції
            routesForStations.computeIfAbsent(data.getStationName(), k -> new HashSet<>())
                    .add(data.getRouteGeneralInfo());
        }

        // Sort and print times for each station and working days
        for (Map.Entry<String, Map<String, ArrayList<Date>>> stationEntry : times.entrySet()) {

            for (Map.Entry<String, ArrayList<Date>> daysEntry : stationEntry.getValue().entrySet()) {
                ArrayList<Date> timeList = daysEntry.getValue();
                Collections.sort(timeList);

                ArrayList<Date> filteredTimes = getDateArrayList(timeList);
                conflicts += filteredTimes.size();
            }

        }
        return conflicts;
    }

    private int countMiniConflicts(ArrayList<Date> times) {
        int conflicts = 0;

        for (int i = 0; i < times.size(); i++) {
            for (int j = i + 1; j < times.size(); j++) {
                long diff = Math.abs(times.get(i).getTime() - times.get(j).getTime());
                if (diff <= 60 * 1000) { // Якщо різниця в часі менше 1 хвилини
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    private DataStructure selectParent() {
        Random rand = new Random();
        DataStructure parent1 = population.get(rand.nextInt(population.size()));
        DataStructure parent2 = population.get(rand.nextInt(population.size()));
        return parent1.getFitness() > parent2.getFitness() ? parent1 : parent2;
    }

    private DataStructure crossover(DataStructure parent1, DataStructure parent2) {
        // Кросовер для створення нащадка на основі двох батьків
        ArrayList<StationData> newRouteData = new ArrayList<>(parent1.getData().size());
        Random rand = new Random();

        for (int i = 0; i < parent1.getData().size(); i++) {
            StationData tmpStationData = parent1.getData().get(i);
            ArrayList<Date> newDates = new ArrayList<>(tmpStationData.getRouteTime().size());
            for (int j = 0; j < tmpStationData.getRouteTime().size(); j++) {
                Date tmpRouteTime = tmpStationData.getRouteTime().get(j);
                if (rand.nextBoolean()) {
                    newDates.add(tmpRouteTime);
                } else {
                    newDates.add(parent2.getData().get(i).getRouteTime().get(j));
                }
            }
            newRouteData.add(new StationData(tmpStationData.getStationName(), tmpStationData.getRouteGeneralInfo(), newDates));
        }

        return new DataStructure(newRouteData);
    }

    private void mutate(DataStructure dataStructure) {
        ArrayList<StationData> stationDataList = dataStructure.getData();
        Random rand = new Random();
        Map<String, Map<String, ArrayList<Date>>> times = loadTimes(stationDataList);

        // Sort and print times for each station and working days
        for (Map.Entry<String, Map<String, ArrayList<Date>>> stationEntry : times.entrySet()) {
            for (Map.Entry<String, ArrayList<Date>> daysEntry : stationEntry.getValue().entrySet()) {
                ArrayList<Date> timeList = daysEntry.getValue();
                Collections.sort(timeList);

                for (int i = 0; i < timeList.size(); i++) {
                    Date currentTime = timeList.get(i);
                    if (i + 1 < timeList.size()) {
                        Date nextTime = timeList.get(i + 1);
                        long interval = (nextTime.getTime() - currentTime.getTime()) / (60 * 1000); // Interval in minutes
                        if (interval < 1) {
                            if (rand.nextBoolean()) {
                                nextTime.setTime(nextTime.getTime() + (rand.nextInt(MAX_INCREMENT_MINUTES / 2) + 1) * 60 * 1000);
                            } else {
                                currentTime.setTime(currentTime.getTime() - (rand.nextInt(MAX_DECREMENT_MINUTES / 2) + 1) * 60 * 1000);
                            }
                        }
                    }
                }
            }
        }

    }

    @Getter
    @Setter
    private class DataStructure {
        private int fitness;
        private ArrayList<StationData> data;

        public DataStructure(ArrayList<StationData> data) {
            this.data = deepCopy(data);
            this.fitness = evaluateFitness(this.data);
        }

        public DataStructure(DataStructure clone) {
            this.data = deepCopy(clone.getData());
            this.fitness = clone.getFitness();
        }

        public void reevaData() {
            this.fitness = evaluateFitness(data);
        }

        private ArrayList<StationData> deepCopy(ArrayList<StationData> original) {
            ArrayList<StationData> copy = new ArrayList<>();
            for (StationData stationData : original) {
                ArrayList<Date> newRouteTimes = new ArrayList<>();
                for (Date date : stationData.getRouteTime()) {
                    newRouteTimes.add(new Date(date.getTime()));
                }
                copy.add(new StationData(stationData.getStationName(), stationData.getRouteGeneralInfo(), newRouteTimes));
            }
            return copy;
        }
    }

}

