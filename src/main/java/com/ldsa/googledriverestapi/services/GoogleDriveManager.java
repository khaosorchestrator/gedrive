package com.ldsa.googledriverestapi.services;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.ldsa.googledriverestapi.config.GoogleDriveConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleDriveManager {

    private final GoogleDriveConfig googleDriveConfig;

    public List<File> findAll() {
        try {
            FileList result = googleDriveConfig
                    .getDrive()
                    .files()
                    .list()
                    .setPageSize(20)
                    .setFields("nextPageToken, files(id, name, size, thumbnailLink, shared)")
                    .execute();
            return result.getFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> findAllInFolder(String parentId) {
        try {
            parentId = parentId == null ? "root" : parentId;
            String query = "'" + parentId + "' in parents";
            FileList result = googleDriveConfig
                    .getDrive()
                    .files()
                    .list()
                    .setQ(query)
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            return result.getFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadFile(String fileId, OutputStream outputStream) {
        if (fileId == null) {
            return;
        }

        try {
            googleDriveConfig
                    .getDrive()
                    .files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Permission setPermission(String type, String role) {
        return new Permission().setType(type).setRole(role);
    }

    public String uploadFile(MultipartFile multipartFile, String folderName, String permissionType, String role) {
        if (multipartFile == null) return null;

        File file = new File();
        file.setParents(Collections.singletonList(getFolderId(folderName)));
        file.setName(multipartFile.getOriginalFilename());

        try {
            File uploadedFile = googleDriveConfig
                    .getDrive()
                    .files()
                    .create(file, new InputStreamContent(multipartFile.getContentType(), new ByteArrayInputStream(multipartFile.getBytes())))
                    .setFields("id")
                    .execute();

            if (!"private".equals(permissionType) && !"private".equals(role)) {
                googleDriveConfig
                        .getDrive()
                        .permissions()
                        .create(uploadedFile.getId(), setPermission(permissionType, role));
            }

            return uploadedFile.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFolderId(String folderName) {
        String parentId = null;

        for (String name : folderName.split("/")) {
            parentId = findOrCreateFolder(parentId, name, googleDriveConfig.getDrive());
        }

        return parentId;
    }

    private String findOrCreateFolder(String parentId, String folderName, Drive drive) {
        String folderId = findFolderById(parentId, folderName, drive);

        if (folderId != null) {
            return folderId;
        }

        File folder = new File();
        folder.setMimeType("application/vnd.google-apps.folder");
        folder.setName(folderName);

        if (parentId != null) {
            folder.setParents(Collections.singletonList(parentId));
        }

        try {
            return drive.files().create(folder).setFields("id").execute().getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String findFolderById(String parentId, String folderName, Drive drive) {
        String folderId = null;
        String pageToken = null;

        do {
            String query = " mimeType = 'application/vnd.google-apps.folder' ";

            query = parentId == null ? query + " and 'root' in parents"  :
                    query + " and '" + parentId + "' in parents";

            try {
                FileList result = drive
                        .files()
                        .list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    if (file.getName().equalsIgnoreCase(folderName)) {
                        folderId = file.getId();
                    }
                }
                pageToken = result.getNextPageToken();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } while (pageToken != null && folderName == null);

        return folderId;
    }

    public void deleteFileOrFolderById(String id) {
        try {
            googleDriveConfig.getDrive().files().delete(id).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
