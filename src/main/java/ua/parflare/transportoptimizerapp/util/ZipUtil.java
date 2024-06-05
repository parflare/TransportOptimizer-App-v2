package ua.parflare.transportoptimizerapp.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipUtil {

    /**
     * Створює zip-архів з вмістом вказаного каталогу.
     *
     * @param sourceDir шлях до каталогу, який потрібно заархівувати
     * @param outputZip шлях до вихідного zip-файлу
     * @throws IOException якщо виникає помилка під час створення архіву
     */
    public void createZipArchive(String sourceDir, String outputZip) throws IOException {
        File directoryToZip = new File(sourceDir);
        try (FileOutputStream fos = new FileOutputStream(outputZip);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zipFile(directoryToZip, directoryToZip.getName(), zos);
        }
    }

    /**
     * Рекурсивно додає файли до zip-архіву.
     *
     * @param fileToZip файл або каталог для архівування
     * @param fileName ім'я файлу або каталогу
     * @param zos об'єкт ZipOutputStream
     * @throws IOException якщо виникає помилка під час додавання файлів до архіву
     */
    private void zipFile(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zos);
                }
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
        }
    }
}
