package ua.parflare.transportoptimizerapp.entity;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class StationData {
    private final String stationName;
    private final String routeGeneralInfo;
    private String routeNumber;
    private String routeName;
    private String routeWorkingDays;

    private ArrayList<Date> routeTime;

    public StationData(String stationName, String routeGeneralInfo, ArrayList<Date> routeTime) {

        stationName = stationName.replaceAll("\\s+", " ");
        if (stationName.endsWith(" ")) {
            stationName = stationName.substring(0, stationName.length() - " ".length());
        }
        if (stationName.startsWith(" ")) {
            stationName = stationName.replaceFirst("^\\s+", "");;
        }
        if (stationName.contains("\"")){
            int firstQuoteIndex = stationName.indexOf("\"");
            // Находим индекс второго вхождения двойных кавычек
            int secondQuoteIndex = stationName.indexOf("\"", firstQuoteIndex + 1);

            // Формируем результат, заменяя двойные кавычки на французские только в найденных позициях
            stationName = stationName.substring(0, firstQuoteIndex) + "«" +
                          stationName.substring(firstQuoteIndex + 1, secondQuoteIndex) + "»" +
                          stationName.substring(secondQuoteIndex + 1);
            //stationName = stationName.replace("\"", "«");
            //stationName = stationName.replace("\"", "»");
        }




        this.stationName = stationName;
        this.routeGeneralInfo = routeGeneralInfo;
        this.routeTime = routeTime;
        extractGeneralInfo(routeGeneralInfo);
    }


    public void combineRouteTime(ArrayList<Date> routeTime) {
        this.routeTime.addAll(routeTime);
        Collections.sort(this.routeTime);
    }

    private void extractGeneralInfo(String info){
        this.routeNumber = getRouteNumber(info);
        this.routeName = getRouteName(info);
        this.routeWorkingDays = getRouteWorkingDays(info);
    }

    private String getRouteNumber(String text) {
        Pattern pattern = Pattern.compile("№\\s*([^\"\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getRouteName(String text) {
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1);
            if (result.endsWith(" ")) {
                result = result.trim();
            }
            return result;
        }
        return null;
    }

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
        routeTime.forEach( date -> {
            sb.append(new SimpleDateFormat("HH:mm").format((date))).append(", ");
        });
        sb.delete(sb.length() - 2, sb.length());
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
