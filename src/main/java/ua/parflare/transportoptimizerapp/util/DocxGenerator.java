package ua.parflare.transportoptimizerapp.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DocxGenerator {

    /**
     * Конвертує список дат у рядок.
     *
     * @param data об'єкт StationData
     * @return рядок з датами у форматі HH:mm
     */
    private static String convertDataListToString(StationData data) {
        StringBuilder sb = new StringBuilder();
        for (Date date : data.getRouteTime()) {
            sb.append(new SimpleDateFormat("HH:mm").format((date))).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    /**
     * Налаштовує властивості документа.
     *
     * @param document об'єкт документа
     */
    public void setDocumentProperties(XWPFDocument document) {
        CTDocument1 doc = document.getDocument();
        CTBody body = doc.getBody();
        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();
        if (!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();
        pageSize.setW(BigInteger.valueOf(11906)); // 11906 твіпів = 21 см = A4
        pageSize.setH(BigInteger.valueOf(16838)); // 16838 твіпів = 29.7 см = A4
    }

    /**
     * Додає текст у документ.
     *
     * @param document об'єкт документа
     * @param text     текст для додавання
     */
    public void createText(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setAlignment(ParagraphAlignment.CENTER); // Вирівнювання посередині
        para.setSpacingBeforeLines(0); // Інтервал перед абзацем
        para.setSpacingAfter(0); // Інтервал після абзацу
        para.setSpacingBetween(1.5); // Міжстроковий інтервал

        XWPFRun run = para.createRun();
        run.setText(text);
        run.addBreak();
        run.setFontFamily("Times New Roman");
        run.setFontSize(14);
    }

    /**
     * Додає таблицю з даними у документ.
     *
     * @param document    об'єкт документа
     * @param stationData список об'єктів StationData для таблиці
     */
    public void createTable(XWPFDocument document, ArrayList<StationData> stationData) {
        XWPFTable table = document.createTable(stationData.size() + 1, 4);
        table.setTableAlignment(TableRowAlign.CENTER);


        XWPFTableRow firstRow = table.getRow(0);
        firstRow.getCell(0).setText("№ маршруту");
        firstRow.getCell(1).setText("Назва маршруту");
        firstRow.getCell(2).setText("Дні роботи");
        firstRow.getCell(3).setText("Розклад руху");


        for (int j = 0; j < stationData.size(); j++) {
            StationData data = stationData.get(j);

            XWPFTableRow secondRow = table.getRow(j + 1);
            secondRow.getCell(0).setText(data.getRouteNumber());
            secondRow.getCell(1).setText(data.getRouteName());
            secondRow.getCell(2).setText(data.getRouteWorkingDays());
            secondRow.getCell(3).setText(convertDataListToString(data));

        }


        mergeCellsInColumns(table);


        table.getRows().forEach(row -> {
            row.getTableCells().forEach(cell -> {
                cell.getParagraphs().forEach(paragraph -> {
                    paragraph.setAlignment(ParagraphAlignment.CENTER);

                    paragraph.setSpacingBetween(1);
                    paragraph.setSpacingAfter(0);
                    paragraph.setSpacingBefore(0);

                    paragraph.getRuns().forEach(run -> {
                        run.setFontFamily("Times New Roman");
                        run.setFontSize(14);
                    });
                });
                cell.setWidthType(TableWidthType.AUTO);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

            });

        });

    }

    /**
     * Об'єднує клітинки у стовпчиках таблиці.
     *
     * @param table об'єкт таблиці
     */
    public void mergeCellsInColumns(XWPFTable table) {
        mergeCellsInColumn(table, 0);
        mergeCellsInColumn(table, 1);

    }

    private void mergeCellsInColumn(XWPFTable table, int columnIndex) {
        Map<String, List<XWPFTableCell>> cellMap = new HashMap<>();

        // Заповнюємо мапу, де ключ - текст комірки першого стовпчика, значення - список комірок з таким текстом
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            XWPFTableCell cell = row.getCell(columnIndex);
            String cellText = cell.getText();
            if (!cellMap.containsKey(cellText)) {
                cellMap.put(cellText, new ArrayList<>());
            }
            cellMap.get(cellText).add(cell);
        }

        // Об'єднуємо комірки з однаковим текстом в першому стовпчику
        for (Map.Entry<String, List<XWPFTableCell>> entry : cellMap.entrySet()) {
            List<XWPFTableCell> cells = entry.getValue();
            if (cells.size() > 1) { // Якщо є більше однієї комірки з таким текстом, об'єднуємо їх
                XWPFTableCell firstCell = cells.get(0);
                for (int i = 1; i < cells.size(); i++) {
                    XWPFTableCell cell = cells.get(i);
                    // Об'єднання комірок
                    firstCell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                }
            }
        }
    }
}
