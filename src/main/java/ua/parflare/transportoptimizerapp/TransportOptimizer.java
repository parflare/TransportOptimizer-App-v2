package ua.parflare.transportoptimizerapp;

import lombok.Getter;
import lombok.Setter;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.util.*;

public class TransportOptimizer {

    private static final int GENERATIONS = 300;
    private static final double MUTATION_RATE = 0.08;
    private static final int MAX_INCREMENT_MINUTES = 4;
    private static final int MAX_DECREMENT_MINUTES = 2;
    private static final int POPULATION_SIZE = 50;
    private ArrayList<DataStructure> population = new ArrayList<>();
    private final DataStructure original;

    private DataStructure maxScoreData;

    public TransportOptimizer(ArrayList<StationData> initialPopulation) {
        original = new DataStructure(initialPopulation);
        maxScoreData = new DataStructure(original);
        population.add(original);
    }

    public ArrayList<StationData> optimizeSchedule() {
        int currentGeneration = 0;
        int maxFitness = initMaxFitness(original.getData());
        int origFitness= evaluateFitness(original.getData());
        ArrayList<DataStructure> newPopulation;
        int tmpFitness = 0;
        //System.out.println();
        while (currentGeneration < GENERATIONS) {

            newPopulation = new ArrayList<>(GENERATIONS);
            newPopulation.add(original);
            while (newPopulation.size() < POPULATION_SIZE) {
                DataStructure parent1 = selectParent();
                DataStructure parent2 = selectParent();
                DataStructure offspring = crossover(parent1, parent2);
                if (Math.random() < MUTATION_RATE) {
                    mutate(offspring);
                }
                if (isWithinAllowedDeviation(offspring)) {
                    newPopulation.add(offspring);
                }
                tmpFitness = offspring.getFitness();
            }
            updateMaxScore(newPopulation);

            population = new ArrayList<>(newPopulation);
            System.out.printf("\rcurrentFitness: %d, maxFitness: %d->%d/%d, iter: %d",  tmpFitness, origFitness, maxScoreData.getFitness(), maxFitness, ++currentGeneration);

        }

        System.out.println();
        System.out.println("Max score: " + maxScoreData.getFitness());

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
            if (data.getFitness() > maxScoreData.getFitness()) {
                maxScoreData = new DataStructure(data);
            }
        }

    }

    private int initMaxFitness(ArrayList<StationData> data){
        int genSize = 0;
        for (int i = 0; i < data.size(); i++) {
            genSize += data.get(i).getRouteTime().size();
        }
        return genSize;
    }

    private int evaluateFitness(ArrayList<StationData> data) {
        int genConf = countConflicts(data);
        int genSize = 0;
        for (int i = 0; i < data.size(); i++) {
            StationData stationData = data.get(i);
            int conflicts = countMiniConflicts(stationData.getRouteTime());
            stationData.setMiniFitness(stationData.getRouteTime().size() - conflicts);
            genSize += stationData.getRouteTime().size();
        }

        return genSize - genConf; // Максимальна кількість балів - кількість конфліктів
    }

    private int countConflicts(ArrayList<StationData> stationDataList) {
        int conflicts = 0;
        Map<String, Map<String, ArrayList<Date>>> times = new HashMap<>();

        // Collect times for each station and working days
        for (StationData data : stationDataList) {
            times.computeIfAbsent(data.getStationName(), k -> new HashMap<>())
                    .computeIfAbsent(data.getRouteWorkingDays(), k -> new ArrayList<>())
                    .addAll(data.getRouteTime());
        }

        // Sort and print times for each station and working days
        for (Map.Entry<String, Map<String, ArrayList<Date>>> stationEntry : times.entrySet()) {
            String stationName = stationEntry.getKey();
            //System.out.println("Station: " + stationName);

            for (Map.Entry<String, ArrayList<Date>> daysEntry : stationEntry.getValue().entrySet()) {
                ArrayList<Date> timeList = daysEntry.getValue();
                Collections.sort(timeList);

                ArrayList<Date> filteredTimes = new ArrayList<>();

                for (int i = 0; i < timeList.size(); i++) {
                    Date currentTime = timeList.get(i);
                    if (i + 1 < timeList.size()) {
                        Date nextTime = timeList.get(i + 1);
                        long interval = (nextTime.getTime() - currentTime.getTime()) / (60 * 1000); // Interval in minutes
                        if (interval < 2) {
                            if (!filteredTimes.contains(currentTime)) {
                                filteredTimes.add(currentTime);
                            }
                            filteredTimes.add(nextTime);
                        }
                    }
                }

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
                if (rand.nextBoolean() && parent1.getData().get(i).getMiniFitness() < parent2.getData().get(i).getMiniFitness()) {
                    newDates.add(tmpRouteTime);
                } else {
                    newDates.add(parent1.getData().get(i).getRouteTime().get(j));
                }
            }
            newRouteData.add(new StationData(tmpStationData.getStationName(), tmpStationData.getRouteGeneralInfo(), newDates));
        }

        return new DataStructure(newRouteData);
    }

    private void mutate(DataStructure dataStructure) {
        ArrayList<StationData> stationData = dataStructure.getData();
        Random rand = new Random();

        for (StationData schedule : stationData) {
            int index = rand.nextInt(schedule.getRouteTime().size());
            Date originalTime = schedule.getRouteTime().get(index);
            Date newTime = new Date(originalTime.getTime() + (rand.nextBoolean() ? 60 * 1000 : -60 * 1000));

            int conflictCount = 0;
            boolean conflict;

            do {
                conflict = false;
                for (Date time : schedule.getRouteTime()) {
                    if (time != originalTime && Math.abs(newTime.getTime() - time.getTime()) < 60 * 1000) {
                        newTime = new Date(newTime.getTime() + (rand.nextBoolean() ? 60 * 1000 : -60 * 1000));
                        conflict = true;
                        conflictCount++;
                        break;
                    }
                }

                if (conflictCount >= 50) {
                    shiftRandomElements(schedule.getRouteTime(), index);
                    conflict = false; // Exit loop after shifting times
                }
            } while (conflict);

            schedule.getRouteTime().set(index, newTime);
        }
    }

    /**
     * Shifts a random number of elements in the list, starting from the specified index, by ±1 minute.
     *
     * @param times  The list of times to shift.
     * @param startIndex The starting index for shifting.
     */
    private void shiftRandomElements(ArrayList<Date> times, int startIndex) {
        Random rand = new Random();
        int numberOfElementsToShift = 2 + rand.nextInt(times.size() - startIndex - 1); // Random number between 2 and the end of the list
        long offset = (rand.nextBoolean() ? 60 * 1000 : -60 * 1000); // ±1 minute

        for (int i = startIndex; i < startIndex + numberOfElementsToShift && i < times.size(); i++) {
            Date shiftedTime = new Date(times.get(i).getTime() + offset);
            times.set(i, shiftedTime);
        }
    }

    @Getter
    @Setter
    private class DataStructure {
        private int fitness;
        private ArrayList<StationData> data;

        public DataStructure(ArrayList<StationData> data) {
            this.data = data;
            this.fitness = evaluateFitness(data);
        }

        public DataStructure(DataStructure clone) {
            this.data = new ArrayList<>(clone.data);
            this.fitness = clone.getFitness();
        }
    }


}

