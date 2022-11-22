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
        List<File> folders = googleDriveManager.findAllInFolder("root");
        List<GoogleDriveFolderDTO> folderDTOS = new ArrayList<>();

        if (folders == null) return folderDTOS;

        folders.forEach(file -> {
            GoogleDriveFolderDTO dto = new GoogleDriveFolderDTO();
            dto.setId(file.getId());
            dto.setName(file.getName());
            dto.setLink("https://drive.google.com/drive/u/3/folders/" + file.getId());
            folderDTOS.add(dto);
        });

        return folderDTOS;
    }

    public void create(String folderName) {
        String folderId = googleDriveManager.getFolderId(folderName);
        System.out.println(folderId);
    }

    public void delete(String id) {
        googleDriveManager.deleteFileOrFolderById(id);
    }
}
