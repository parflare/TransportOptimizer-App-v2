package ua.parflare.transportoptimizerapp.util;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipUtil {

    public void createZipArchive(Map<String, byte[]> files, OutputStream out) throws IOException {
        int totalFiles = files.size();
        int processedFiles = 0;

        System.out.println();
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                processedFiles++;
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(entry.getValue())) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }

                // Виводимо прогрес у консоль
                System.out.printf("\rProcessed [%d/%d] files",
                        processedFiles, totalFiles);
            }
        }
    }

}
