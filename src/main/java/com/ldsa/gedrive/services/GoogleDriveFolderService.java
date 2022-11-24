package com.ldsa.gedrive.services;

import com.google.api.services.drive.model.File;
import com.google.common.io.ByteStreams;
import com.ldsa.gedrive.dtos.GoogleDriveFolderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class GoogleDriveFolderService {

    private final GoogleDriveManager googleDriveManager;

    public List<GoogleDriveFolderDTO> findAll() {
        List<File> folders = googleDriveManager.findAllInFolderById("root");
        List<GoogleDriveFolderDTO> googleDriveFolderDTOS = new ArrayList<>();

        if (folders == null) return googleDriveFolderDTOS;

        folders.forEach(folder -> {
            GoogleDriveFolderDTO dto = new GoogleDriveFolderDTO();
            dto.setId(folder.getId());
            dto.setName(folder.getName());
            dto.setLink("https://drive.google.com/drive/u/3/folders/" + folder.getId());
            googleDriveFolderDTOS.add(dto);
        });

        return googleDriveFolderDTOS;
    }

    public void create(String folderName) {
        String folderId = googleDriveManager.getFolderId(folderName);
        System.out.println(folderId);
    }

    public String getFolderId(String folderName) {
        return googleDriveManager.getFolderId(folderName);
    }

    public void delete(String id) {
        googleDriveManager.deleteFileOrFolderById(id);
    }

    public byte[] download(String folderId, OutputStream outputStream) {
        List<File> folders = googleDriveManager.findAllInFolderById(folderId);
        return zipFiles(folders, outputStream);
    }

    private byte[] zipFiles(List<File> files, OutputStream outputStream) {

        byte[] result = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (File file : files) {
                OutputStream out = googleDriveManager.downloadFolderAsZip(file.getId(), outputStream);
                byte[] downloadedBytes = new byte[file.size()];
                out.write(downloadedBytes);
                InputStreamSource source =  new ByteArrayResource(downloadedBytes);

                try (InputStream fileInputStream = source.getInputStream()) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    ByteStreams.copy(fileInputStream, zipOutputStream);
                }
            }

            zipOutputStream.close();
            byteArrayOutputStream.close();
            result = byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
