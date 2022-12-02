package com.ldsa.gedrive.services;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.ldsa.gedrive.config.GoogleDriveConfig;
import com.ldsa.gedrive.utils.PermissionDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
                    .setFields("nextPageToken, files(id, name, size, thumbnailLink, shared)")
                    .execute();
            return result.getFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> findAllInFolderById(String folderId) {
        try {
            folderId = folderId == null ? "root" : folderId;
            String query = "'" + folderId + "' in parents";
            FileList result = googleDriveConfig
                    .getDrive()
                    .files()
                    .list()
                    .setQ(query)
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, size, thumbnailLink, shared)")
                    .execute();
            return result.getFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void download(String fileId, OutputStream outputStream) {
        try {
            googleDriveConfig.getDrive().files().get(fileId).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Permission setPermission(PermissionDetails permissionDetails) {
        Permission permission = new Permission();

        if (!permissionDetails.getEmailAddress().isEmpty()) {
            permission.setEmailAddress(permissionDetails.getEmailAddress());
        }

        return permission
                .setType(permissionDetails.getType())
                .setRole(permissionDetails.getRole());
    }

    public String uploadFile(MultipartFile multipartFile, String folderName, PermissionDetails permissionDetails) {
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

            if (!"private".equals(permissionDetails.getType()) && !"private".equals(permissionDetails.getRole())) {
                googleDriveConfig
                        .getDrive()
                        .permissions()
                        .create(uploadedFile.getId(), setPermission(permissionDetails));
            }

            return uploadedFile.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFolderId(String folderName) {
        String parentId = null;

        for (String name : folderName.split("/")) {
            parentId = findOrCreateFolder(parentId, name);
        }

        return parentId;
    }

    private String findOrCreateFolder(String parentId, String folderName) {
        String folderId = findFolderById(parentId, folderName);

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
            return googleDriveConfig.getDrive()
                    .files().create(folder).setFields("id").execute().getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String findFolderById(String parentId, String folderName) {
        String folderId = null;
        String pageToken = null;

        do {
            String query = " mimeType = 'application/vnd.google-apps.folder' ";

            query = parentId == null ? query + " and 'root' in parents"  :
                    query + " and '" + parentId + "' in parents";

            try {
                FileList result = googleDriveConfig.getDrive()
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

    public InputStream getFileAsInputStream(String fileID) {
        try {
            return googleDriveConfig.getDrive()
                    .files()
                    .get(fileID)
                    .executeMediaAsInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copy(String fileId, String folderName) {
        String folderId = getFolderId(folderName);

        if (folderId == null) {
            throw new RuntimeException("Folder " + folderName + " not found.");
        }

        try {
            googleDriveConfig.getDrive()
                    .files()
                    .copy(fileId, new File().setParents(List.of(folderId)))
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(String fileId, String folderName) {
        String folderId = getFolderId(folderName);

        if (folderId == null) {
            throw new RuntimeException("Folder " + folderName + " not found.");
        }

        try {
            File folder = googleDriveConfig.getDrive().files().get(folderId).execute();
            googleDriveConfig.getDrive().files().update(fileId, folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
