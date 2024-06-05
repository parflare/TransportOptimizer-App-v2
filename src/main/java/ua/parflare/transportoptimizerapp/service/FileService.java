package ua.parflare.transportoptimizerapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.parflare.transportoptimizerapp.entity.StationData;
import ua.parflare.transportoptimizerapp.util.DocxGenerator;
import ua.parflare.transportoptimizerapp.util.ExcelProcessor;
import ua.parflare.transportoptimizerapp.util.ZipUtil;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final String FILE_DIRECTORY = "src/main/resources/static/files";

    private final ExcelProcessor excelProcessor;
    private final DocxGenerator docxGenerator;
    private final ZipUtil zipUtil;

    public String saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (!isExcelFile(fileName)) {
            throw new IOException(fileName + " has invalid file format (requires Excel)");
        }

        File folder = new File(FILE_DIRECTORY);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        File savedFile = new File(folder, uniqueFileName);
        try (OutputStream outputStream = new FileOutputStream(savedFile);
             InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return "File uploaded successfully: " + uniqueFileName;
    }

    public String processAndGenerateReport(MultipartFile file) throws IOException {
        List<StationData> stationData = excelProcessor.processExcelFile(file);

        String reportFileName = "report_" + System.currentTimeMillis() + ".docx";
        String reportFilePath = FILE_DIRECTORY + File.separator + reportFileName;

        docxGenerator.generateDocxReport(stationData, reportFilePath);

        return "Report generated successfully: " + reportFileName;
    }

    public String createZipWithReports() throws IOException {
        String zipFileName = FILE_DIRECTORY + File.separator + "reports_" + System.currentTimeMillis() + ".zip";
        zipUtil.createZipArchive(FILE_DIRECTORY, zipFileName);
        return "Zip archive created successfully: " + zipFileName;
    }

    private boolean isExcelFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".xlsm");
    }
}
