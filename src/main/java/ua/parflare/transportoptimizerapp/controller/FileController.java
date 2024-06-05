package ua.parflare.transportoptimizerapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.parflare.transportoptimizerapp.service.FileService;

import java.io.*;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String responseMessage = fileService.saveFile(file);
            return ResponseEntity.ok(responseMessage);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file");
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> processFile(@RequestParam("file") MultipartFile file) {
        try {
            String responseMessage = fileService.processAndGenerateReport(file);
            return ResponseEntity.ok(responseMessage);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process file");
        }
    }

    @GetMapping("/create-zip")
    public ResponseEntity<String> createZip() {
        try {
            String responseMessage = fileService.createZipWithReports();
            return ResponseEntity.ok(responseMessage);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to create zip archive");
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadReports() {
        // Реалізація завантаження звітів
        return null;
    }
}
