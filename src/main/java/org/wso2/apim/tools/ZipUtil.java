package org.wso2.apim.tools;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility to create a Zip file
 */
public class ZipUtil {
    /**
     * Archive a provided source directory to a zipped file
     *
     * @param sourceDirectory Source directory
     */
    public static void archiveDirectory(String sourceDirectory) throws Exception {

        File directoryToZip = new File(sourceDirectory);

        List<File> fileList = new ArrayList<File>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList);


    }

    /**
     * Retrieve all the files included in the source directory to be archived
     *
     * @param sourceDirectory Source directory
     * @param fileList        List of files
     */
    private static void getAllFiles(File sourceDirectory, List<File> fileList) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                }
            }
        }
    }

    /**
     * Generate archive file
     *
     * @param directoryToZip Location of the archive
     * @param fileList       List of files to be included in the archive
     */
    private static void writeArchiveFile(File directoryToZip, List<File> fileList) throws Exception {

        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(directoryToZip.getPath() + ".zip");
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }

        } catch (IOException e) {

            throw new Exception("I/O error while adding files to archive", e);
        } finally {
            zipOutputStream.close();
            fileOutputStream.close();
        }
    }

    /**
     * Add files of the directory to the archive
     *
     * @param directoryToZip  Location of the archive
     * @param file            File to be included in the archive
     * @param zipOutputStream Output stream
     */
    private static void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
            throws Exception {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            // Get relative path from archive directory to the specific file
            String zipFilePath = file.getCanonicalPath()
                    .substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
            zipFilePath = zipFilePath.replace(File.separatorChar, '/');
            ZipEntry zipEntry = new ZipEntry(directoryToZip.getName() + "/" + zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);

            IOUtils.copy(fileInputStream, zipOutputStream);

            zipOutputStream.closeEntry();
        } catch (IOException e) {

            throw new Exception("I/O error while writing files to archive", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }
}
