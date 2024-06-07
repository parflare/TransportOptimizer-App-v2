package ua.parflare.transportoptimizerapp;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.parflare.transportoptimizerapp.entity.StationData;
import ua.parflare.transportoptimizerapp.util.ExcelProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

public class Main {

    public static void main(String[] args)  {
        ArrayList<StationData> stationDataList = initializeStationData();

        createExcelFile(stationDataList, "orig");
        printSortedTimes(stationDataList);

        System.out.println("\nwas orig");
        TransportOptimizer ga = new TransportOptimizer(stationDataList);
        ArrayList<StationData> optimizedSchedule = ga.optimizeSchedule();

        System.out.println("\n\n\n");

        createExcelFile(optimizedSchedule, "opt");
        printSortedTimes(optimizedSchedule);
        System.out.println("\nwas new");


    }

    // ініціалізація
    static ArrayList<StationData> initializeStationData() {
        ArrayList<StationData> stationDataList = null;
        try {
            stationDataList = processExcelFile("C:\\Users\\Olexandr\\Desktop\\555.xlsx", 9999);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Initialize stationDataList with your data
        return stationDataList;
    }

    public static void createExcelFile(ArrayList<StationData> stationDataList, String name) {
        try (Workbook writeWorkbook = new XSSFWorkbook()) {
            Sheet writeSheet = writeWorkbook.createSheet("Combined Data");

            for (int i = 0; i < stationDataList.size(); i++) {
                Row row = writeSheet.createRow(i + 1);
                StationData stationData = stationDataList.get(i);

                row.createCell(0).setCellValue(stationData.getStationName());
                row.createCell(1).setCellValue(stationData.getRouteGeneralInfo());

                ArrayList<Date> dates = stationData.getRouteTime();
                for (int j = 0; j < dates.size(); j++) {
                    Cell cell = row.createCell(j + 2);
                    cell.setCellValue(new SimpleDateFormat("HH:mm").format((dates.get(j))));
                    CellStyle cellStyle = writeWorkbook.createCellStyle();
                    CreationHelper createHelper = writeWorkbook.getCreationHelper();
                    cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
                    cell.setCellStyle(cellStyle);
                }
            }

            writeWorkbookToFile(writeWorkbook, "files/" + name + ".xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeWorkbookToFile(Workbook workbook, String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Створити всі необхідні директорії
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new IOException("Помилка при записі Excel файлу: " + e.getMessage(), e);
        } finally {
            // Обов'язково закриваємо workbook, щоб звільнити ресурси
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    public static ArrayList<StationData> processExcelFile(String filePath, int maxElements) throws IOException {
        ArrayList<StationData> data;

        try (Workbook readWorkbook = WorkbookFactory.create(new FileInputStream(filePath))) {
            Sheet readSheet = readWorkbook.getSheet("Combined Data");
            data = new ArrayList<>(readSheet.getLastRowNum());

            for (int j = 1; j <= readSheet.getLastRowNum() && j < maxElements; j++) {
                Row row = readSheet.getRow(j);

                String stationName = row.getCell(0).getStringCellValue();
                String routeInfo = row.getCell(1).getStringCellValue();
                ArrayList<Date> dates = new ArrayList<>(row.getLastCellNum() - 2);

                for (int i = 2; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellType().equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)) {
                        Date time = cell.getDateCellValue();
                        dates.add(time);
                    }
                }
                data.add(new StationData(stationName, routeInfo, dates));
            }

        } catch (IOException e) {
            throw new IOException("Помилка при зчитуванні Excel файлу: " + e.getMessage(), e);
        }


        return data;
    }

    private static void printSortedTimes(List<StationData> stationDataList) {
        Map<String, Map<String, List<Date>>> times = new HashMap<>();

        // Collect times for each station and working days
        for (StationData data : stationDataList) {
            times.computeIfAbsent(data.getStationName(), k -> new HashMap<>())
                    .computeIfAbsent(data.getRouteWorkingDays(), k -> new ArrayList<>())
                    .addAll(data.getRouteTime());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        // Sort and print times for each station and working days
        for (Map.Entry<String, Map<String, List<Date>>> stationEntry : times.entrySet()) {
            String stationName = stationEntry.getKey();
            System.out.println("Station: " + stationName);

            for (Map.Entry<String, List<Date>> daysEntry : stationEntry.getValue().entrySet()) {
                String workingDays = daysEntry.getKey();
                List<Date> timeList = daysEntry.getValue();
                Collections.sort(timeList);

                List<Date> filteredTimes = filterTimes(timeList);

                System.out.print(workingDays + ": ");
                for (Date time : filteredTimes) {
                    System.out.print(sdf.format(time) + ", ");
                }
                System.out.println();
            }
        }
    }

    /**
     * Filters times to include only those that match or have an interval of less than 2 minutes.
     *
     * @param timeList the list of times to filter
     * @return the filtered list of times
     */
    private static List<Date> filterTimes(List<Date> timeList) {
        List<Date> filteredTimes = new ArrayList<>();
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
        return filteredTimes;
    }

}
