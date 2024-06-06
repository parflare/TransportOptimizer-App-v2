package ua.parflare.transportoptimizerapp.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.parflare.transportoptimizerapp.entity.StationData;
import ua.parflare.transportoptimizerapp.util.DocxGenerator;
import ua.parflare.transportoptimizerapp.util.ExcelProcessor;
import ua.parflare.transportoptimizerapp.util.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FileService {

    private final ExcelProcessor excelProcessor;
    private final DocxGenerator docxGenerator;
    private final ZipUtil zipUtil;

    // Map для збереження файлів у пам'яті по імені користувача
    private final Map<String, byte[]> userFileStorage = new ConcurrentHashMap<>();
    private final Map<String, Map<String, byte[]>> userReportStorage = new ConcurrentHashMap<>();


    public String saveFile(String username, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        assert fileName != null;
        if (!isExcelFile(fileName)) {
            throw new IOException(fileName + " has invalid file format (requires Excel)");
        }

        // Зчитуємо байти з MultipartFile
        byte[] fileBytes = file.getBytes();

        // Зберігаємо байти у мапі
        userFileStorage.put(username, fileBytes);

        return "File " + fileName + " uploaded successfully";
    }

    private ArrayList<StationData> optimize(byte[] file) throws IOException {
        return excelProcessor.processExcelFile(file);
    }

    public String processAndGenerateReport(String username) throws IOException {
        var file = userFileStorage.get(username);

        if (file == null) {
            throw new IOException("File not found in memory for user " + username);
        }

        ArrayList<StationData> tmpData = optimize(file);

        userReportStorage
                .computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                .put("!new_data.xlsx", excelProcessor.createExcelFile(tmpData));

        ArrayList<StationData> stationData = combineStation(tmpData);

        ArrayList<String> stationNames = getUniqueStationNames(stationData);

        Collections.sort(stationNames);

        int totalIteration = stationNames.size();

//        for (var name : stationData) {
//            System.out.println(name.getStationName());
//        }

        System.out.println();

        int faultCounter = 0, successCounter = 0;
        for (int i = 0; i < totalIteration; i++) {
            if (createDocx(stationNames.get(i), stationData, username)) {
                successCounter++;
            } else {
                faultCounter++;
            }
            System.out.printf("\rОброблено [%d/%d] - %d\uD83D\uDC4D|%d\uD83D\uDC4E",
                    i + 1, totalIteration, successCounter, faultCounter);
        }

        return "Reports generated successfully!";
    }

    private boolean createDocx(String search, ArrayList<StationData> data, String username) {
        String fileName = search + ".docx";

        try (XWPFDocument document = new XWPFDocument()) {
            docxGenerator.setDocumentProperties(document);
            docxGenerator.createText(document, String.format("Зупинка «%s»", search));
            docxGenerator.createTable(document, getStationDataByName(search, data));

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.write(out);

                // Отримуємо байти з ByteArrayOutputStream
                byte[] docxBytes = out.toByteArray();

                // Зберігаємо байти документа у ConcurrentHashMap
                userReportStorage
                        .computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                        .put(fileName, docxBytes);

                //System.out.println("\rДокумент Word успішно створено та збережено в пам'яті: " + fileName);
            }

            return true;

        } catch (Exception e) {
            System.err.println("\rПомилка у " + fileName);
            return false;
        }
    }

    public ArrayList<StationData> getStationDataByName(String name, ArrayList<StationData> stationData) {
        ArrayList<StationData> result = new ArrayList<>();
        for (StationData data : stationData) {
            if (data.getStationName().equals(name)) {
                result.add(data);
            }
        }
        //System.out.println(result);
        return result;
    }

    public ArrayList<StationData> combineStation(ArrayList<StationData> stationData) {
        ArrayList<StationData> uniqueStationData = new ArrayList<>();

        for (StationData data : stationData) {
            if (uniqueStationData.contains(data)) {
                int index = uniqueStationData.indexOf(data);
                uniqueStationData.get(index).combineRouteTime(data.getRouteTime());
            } else {
                uniqueStationData.add(data);
            }
        }
        return uniqueStationData;
    }

    private ArrayList<String> getUniqueStationNames(ArrayList<StationData> stationData) {
        HashSet<String> stationNames = new HashSet<>();
        for (StationData data : stationData) {
            stationNames.add(data.getStationName());
        }
        return new ArrayList<>(stationNames);
    }

    public byte[] createZipWithReports(String username) throws IOException {
        Map<String, byte[]> userReports = userReportStorage.get(username);
        if (userReports == null || userReports.isEmpty()) {
            throw new IOException("No reports found for user " + username);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            zipUtil.createZipArchive(userReports, baos);

            // Отримання мапи для користувача і видалення її з пам'яті
            userReportStorage.remove(username);

            return baos.toByteArray();
        }
    }


    private boolean isExcelFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return extension.equalsIgnoreCase(".xls") || extension.equalsIgnoreCase(".xlsx");
    }
}