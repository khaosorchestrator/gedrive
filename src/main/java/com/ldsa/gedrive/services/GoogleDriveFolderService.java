package com.ldsa.gedrive.services;

import com.google.api.services.drive.model.File;
import com.google.common.io.ByteStreams;
import com.ldsa.gedrive.dtos.GoogleDriveFolderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    public byte[] download(String folderId) {
        List<File> folders = googleDriveManager.findAllInFolderById(folderId);
        return zipFiles(folders);
    }

    private byte[] zipFiles(List<File> files) {

        byte[] result = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (File file : files) {
                try (InputStream fileInputStream = googleDriveManager.getFileAsInputStream(file.getId())) {
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
