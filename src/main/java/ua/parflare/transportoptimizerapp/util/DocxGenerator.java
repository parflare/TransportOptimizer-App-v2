package ua.parflare.transportoptimizerapp.util;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;
import ua.parflare.transportoptimizerapp.entity.StationData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class DocxGenerator {

    private static final String DATE_FORMAT = "HH:mm";
    private static final int A4_WIDTH_TWIPS = 11906;
    private static final int A4_HEIGHT_TWIPS = 16838;

    /**
     * Генерує DOCX звіт з даними.
     *
     * @param data список об'єктів StationData для звіту
     * @param outputPath шлях до вихідного файлу
     */
    public void generateDocxReport(List<StationData> data, String outputPath) {
        try (XWPFDocument document = new XWPFDocument()) {
            // Налаштування документа
            setDocumentProperties(document);

            // Додавання тексту
            createText(document, "Звіт");

            // Додавання таблиці з даними
            createTable(document, data);

            // Запис документа у файл
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
                System.out.println("Документ успішно створено: " + outputPath);
            }
        } catch (IOException e) {
            System.err.println("Помилка при створенні документа: " + e.getMessage());
        }
    }

    /**
     * Налаштовує властивості документа.
     *
     * @param document об'єкт документа
     */
    private void setDocumentProperties(XWPFDocument document) {
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
        pageSize.setW(BigInteger.valueOf(A4_WIDTH_TWIPS));
        pageSize.setH(BigInteger.valueOf(A4_HEIGHT_TWIPS));
    }

    /**
     * Додає текст у документ.
     *
     * @param document об'єкт документа
     * @param text     текст для додавання
     */
    private void createText(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setAlignment(ParagraphAlignment.CENTER);
        para.setSpacingBeforeLines(0);
        para.setSpacingAfter(0);
        para.setSpacingBetween(1.5);

        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontFamily("Times New Roman");
        run.setFontSize(14);
        run.addBreak();
    }

    /**
     * Додає таблицю з даними у документ.
     *
     * @param document об'єкт документа
     * @param stationData список об'єктів StationData для таблиці
     */
    private void createTable(XWPFDocument document, List<StationData> stationData) {
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

        table.getRows().forEach(row -> row.getTableCells().forEach(cell -> {
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
        }));
    }

    /**
     * Конвертує список дат у рядок.
     *
     * @param data об'єкт StationData
     * @return рядок з датами у форматі HH:mm
     */
    private String convertDataListToString(StationData data) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        for (Date date : data.getRouteTime()) {
            sb.append(sdf.format(date)).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
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

        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            XWPFTableCell cell = row.getCell(columnIndex);
            String cellText = cell.getText();
            cellMap.computeIfAbsent(cellText, k -> new ArrayList<>()).add(cell);
        }

        for (Map.Entry<String, List<XWPFTableCell>> entry : cellMap.entrySet()) {
            List<XWPFTableCell> cells = entry.getValue();
            if (cells.size() > 1) {
                XWPFTableCell firstCell = cells.get(0);
                for (int i = 1; i < cells.size(); i++) {
                    XWPFTableCell cell = cells.get(i);
                    firstCell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                }
            }
        }
    }
}
