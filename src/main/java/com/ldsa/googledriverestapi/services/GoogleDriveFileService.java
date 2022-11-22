package com.ldsa.googledriverestapi.services;

import com.google.api.services.drive.model.File;
import com.ldsa.googledriverestapi.dtos.GoogleDriveFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleDriveFileService {

    private final GoogleDriveFileServiceManager googleDriveFileServiceManager;

    public List<GoogleDriveFileDTO> findAll() {

        List<GoogleDriveFileDTO> googleDriveFileDTOS = new ArrayList<>();
        List<File> files = googleDriveFileServiceManager.findAll();

        if (files == null) return googleDriveFileDTOS;

        GoogleDriveFileDTO driveFileDto = new GoogleDriveFileDTO();

        files.forEach(file -> {
            if (file.getSize() != null) {
                driveFileDto.setId(file.getId());
                driveFileDto.setName(file.getName());
                driveFileDto.setThumbnailLink(file.getThumbnailLink());
                driveFileDto.setSize(String.valueOf(file.getSize()));
                driveFileDto.setLink("https://drive.google.com/file/d/" + file.getId() + "/view?usp=sharing");
                driveFileDto.setShared(file.getShared());
                googleDriveFileDTOS.add(driveFileDto);
            }
        });

        return googleDriveFileDTOS;
    }

    public void deleteById(String fileId) {
        googleDriveFileServiceManager.deleteFileOrFolderById(fileId);
    }
}
