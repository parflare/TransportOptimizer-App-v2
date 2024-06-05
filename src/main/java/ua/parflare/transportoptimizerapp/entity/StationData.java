package ua.parflare.transportoptimizerapp.entity;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class StationData {

    private final String stationName;
    private final String routeGeneralInfo;
    private String routeNumber;
    private String routeName;
    private String routeWorkingDays;
    private final ArrayList<Date> routeTime;

    /**
     * Конструктор для StationData.
     *
     * @param stationName назва станції
     * @param routeGeneralInfo загальна інформація про маршрут
     * @param routeTime часи маршруту
     */
    public StationData(String stationName, String routeGeneralInfo, ArrayList<Date> routeTime) {
        this.stationName = formatStationName(stationName);
        this.routeGeneralInfo = routeGeneralInfo;
        this.routeTime = new ArrayList<>(routeTime);
        extractGeneralInfo(routeGeneralInfo);
    }

    /**
     * Об'єднує нові часи маршруту з існуючими та сортує їх.
     *
     * @param routeTime нові часи маршруту
     */
    public void combineRouteTime(ArrayList<Date> routeTime) {
        this.routeTime.addAll(routeTime);
        Collections.sort(this.routeTime);
    }

    /**
     * Форматує назву станції.
     *
     * @param stationName назва станції
     * @return відформатована назва станції
     */
    private String formatStationName(String stationName) {
        stationName = stationName.trim().replaceAll("\\s+", " ");
        if (stationName.contains("\"")) {
            int firstQuoteIndex = stationName.indexOf("\"");
            int secondQuoteIndex = stationName.indexOf("\"", firstQuoteIndex + 1);
            if (secondQuoteIndex > firstQuoteIndex) {
                stationName = stationName.substring(0, firstQuoteIndex) + "«" +
                              stationName.substring(firstQuoteIndex + 1, secondQuoteIndex) + "»" +
                              stationName.substring(secondQuoteIndex + 1);
            }
        }
        return stationName;
    }

    /**
     * Витягує загальну інформацію про маршрут.
     *
     * @param info загальна інформація про маршрут
     */
    private void extractGeneralInfo(String info) {
        this.routeNumber = getRouteNumber(info);
        this.routeName = getRouteName(info);
        this.routeWorkingDays = getRouteWorkingDays(info);
    }

    /**
     * Витягує номер маршруту з тексту.
     *
     * @param text текст з інформацією про маршрут
     * @return номер маршруту
     */
    private String getRouteNumber(String text) {
        Pattern pattern = Pattern.compile("№\\s*([^\"\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Витягує назву маршруту з тексту.
     *
     * @param text текст з інформацією про маршрут
     * @return назва маршруту
     */
    private String getRouteName(String text) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * Витягує дні роботи маршруту з тексту.
     *
     * @param text текст з інформацією про маршрут
     * @return дні роботи маршруту
     */
    private String getRouteWorkingDays(String text) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(text);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }
        return lastMatch;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StationData{");
        sb.append("stationName='").append(stationName).append('\'');
        sb.append(", routeInfo='").append(routeGeneralInfo).append('\'');
        sb.append(", routeTime=[");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        routeTime.forEach(date -> sb.append(sdf.format(date)).append(", "));
        if (!routeTime.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationData that = (StationData) o;
        return Objects.equals(stationName, that.stationName)
               && Objects.equals(routeNumber, that.routeNumber)
               && Objects.equals(routeName, that.routeName)
               && Objects.equals(routeWorkingDays, that.routeWorkingDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationName, routeNumber, routeName, routeWorkingDays);
    }
}
