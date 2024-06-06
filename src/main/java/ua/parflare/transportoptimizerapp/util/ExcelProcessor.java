package ua.parflare.transportoptimizerapp.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Component
public class ExcelProcessor {
    public ArrayList<StationData> processExcelFile(byte[] fileBytes) throws IOException {
        ArrayList<StationData> data;

        try (Workbook readWorkbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet readSheet = readWorkbook.getSheet("Combined Data");
            data = new ArrayList<>(readSheet.getLastRowNum());

            for (int j = 1; j <= readSheet.getLastRowNum(); j++) {
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

    public byte[] createExcelFile(ArrayList<StationData> stationDataList) throws IOException {
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

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writeWorkbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

}
