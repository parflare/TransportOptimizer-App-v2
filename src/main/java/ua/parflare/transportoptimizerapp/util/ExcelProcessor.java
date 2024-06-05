package ua.parflare.transportoptimizerapp.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ExcelProcessor {

    /**
     * Обробляє Excel файл та повертає список об'єктів StationData.
     *
     * @param file файл Excel для обробки
     * @return список об'єктів StationData
     * @throws IOException якщо виникає помилка під час зчитування файлу
     */
    public List<StationData> processExcelFile(MultipartFile file) throws IOException {
        List<StationData> stationDataList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Читаємо перший лист у книзі

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Починаємо з 1, щоб пропустити заголовок
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String stationName = getCellValueAsString(row.getCell(0));
                String routeInfo = getCellValueAsString(row.getCell(1));
                List<Date> routeTimes = new ArrayList<>();

                for (int j = 2; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        routeTimes.add(cell.getDateCellValue());
                    }
                }

                stationDataList.add(new StationData(stationName, routeInfo, new ArrayList<>(routeTimes)));
            }
        } catch (IOException e) {
            throw new IOException("Помилка при зчитуванні Excel файлу: " + e.getMessage(), e);
        }

        return stationDataList;
    }

    /**
     * Отримує значення клітинки у вигляді рядка.
     *
     * @param cell клітинка для обробки
     * @return значення клітинки у вигляді рядка
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                return cell.getCellFormula().trim();
            default:
                return "";
        }
    }
}
