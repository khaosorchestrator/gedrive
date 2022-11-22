package com.ldsa.gedrive.services;

import com.google.api.services.drive.model.File;
import com.ldsa.gedrive.dtos.GoogleDriveFolderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleDriveFolderService {

    private final GoogleDriveManager googleDriveManager;

    public List<GoogleDriveFolderDTO> findAll() {
        List<File> folders = googleDriveManager.findAllInFolderById("root");
        List<GoogleDriveFolderDTO> googleDriveFolderDTOS = new ArrayList<>();

        if (folders == null) return googleDriveFolderDTOS;

        folders.forEach(file -> {
            GoogleDriveFolderDTO dto = new GoogleDriveFolderDTO();
            dto.setId(file.getId());
            dto.setName(file.getName());
            dto.setLink("https://drive.google.com/drive/u/3/folders/" + file.getId());
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
}
