package com.ldsa.gedrive.services;

import com.google.api.services.drive.model.File;
import com.ldsa.gedrive.dtos.GoogleDriveFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleDriveFileService {

    private final GoogleDriveManager googleDriveManager;

    public List<GoogleDriveFileDTO> findAll() {

        List<GoogleDriveFileDTO> googleDriveFileDTOS = new ArrayList<>();
        List<File> files = googleDriveManager.findAll();

        if (files == null) return googleDriveFileDTOS;

        GoogleDriveFileDTO driveFileDto = new GoogleDriveFileDTO();

        files.forEach(file -> {
            if (file.getSize() != null) {
                fillGoogleDriveFileDTOList(googleDriveFileDTOS, file, driveFileDto);
            }
        });

        return googleDriveFileDTOS;
    }

    public List<GoogleDriveFileDTO> findAllInFolder(String folderId) {

        List<GoogleDriveFileDTO> googleDriveFileDTOList = new ArrayList<>();
        List<File> files = googleDriveManager.findAllInFolderById(folderId);

        if (files == null) return googleDriveFileDTOList;

        files.forEach(file -> {
            if (file.getSize() != null) {
                GoogleDriveFileDTO driveFileDto = new GoogleDriveFileDTO();
                fillGoogleDriveFileDTOList(googleDriveFileDTOList, file, driveFileDto);
            }
        });

        return googleDriveFileDTOList;
    }

    private void fillGoogleDriveFileDTOList(List<GoogleDriveFileDTO> googleDriveFileDTOS, File file, GoogleDriveFileDTO driveFileDto) {
        driveFileDto.setId(file.getId());
        driveFileDto.setName(file.getName());
        driveFileDto.setThumbnailLink(file.getThumbnailLink());
        driveFileDto.setSize(String.valueOf(file.getSize()));
        driveFileDto.setLink("https://drive.google.com/file/d/" + file.getId() + "/view?usp=sharing");
        driveFileDto.setShared(file.getShared());
        googleDriveFileDTOS.add(driveFileDto);
    }

    public void deleteById(String fileId) {
        googleDriveManager.deleteFileOrFolderById(fileId);
    }

    public String upload(MultipartFile file, String path, boolean isPublic) {
        String permissionType;
        String role;

        if (isPublic) {
            permissionType = "anyone";
            role = "reader";
        } else {
            permissionType = "private";
            role = "private";
        }

        return googleDriveManager.uploadFile(file, path, permissionType, role);
    }

    public void download(String fileId, OutputStream outputStream) {
        googleDriveManager.download(fileId, outputStream);
    }
}
