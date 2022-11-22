package com.ldsa.googledriverestapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class HomeController {

    private static final String APPLICATION_NAME = "Google Drive Rest API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_APPDATA);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private final NetHttpTransport HTTP_TRANSPORT;
    private final GoogleAuthorizationCodeFlow flow;

    private HomeController() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = HomeController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                GoogleClientSecrets
                        .load(JSON_FACTORY, new InputStreamReader(in)), SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    @GetMapping
    public String home() throws Exception {
        boolean isUserAuthenticated = false;
        Credential credential = getCredentials();

        if (credential != null) {
            boolean tokenValid = credential.refreshToken();

            if (tokenValid) {
                isUserAuthenticated = true;
            }
        }

        return isUserAuthenticated ? "dashboard.html" : "index.html";
    }

    @GetMapping("/google-sign-in")
    public void sign(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri("").setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping("/oauth")
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            savedToken(code);
            return "dashboard.html";
        }

        return "index.html";
    }

    private void savedToken(String code) throws Exception {
        String OAUTH_URI = "http://localhost:2424";
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(OAUTH_URI).execute();
        flow.createAndStoreCredential(response, "user");
    }

    @GetMapping("/create")
    public void createFile(HttpServletResponse response) throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME).build();

        File file = new File();
        file.setName("profile.jpg");

        FileContent content = new FileContent("image/jpeg", new java.io.File(""));
        File uploadedFile = drive.files().create(file, content).setFields("id").execute();

        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
        response.getWriter().write(fileReference);
    }

    @PostMapping("/upload-in-folder")
    public void uploadFileInFolder(HttpServletResponse response, MultipartFile requestFile) throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build();

        File file = new File();
        file.setName("");
        file.setParents(List.of(""));

        FileContent content = new FileContent("image/jpeg", new java.io.File(""));
        File uploadedFile = drive.files().create(file, content).setFields("id").execute();

        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
        response.getWriter().write(fileReference);
    }

    @GetMapping("/list-files")
    public ResponseEntity<List<FileOutputDto>> listFiles() throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build();

        List<FileOutputDto> list = new ArrayList<>();

        FileList result = drive.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)").execute();

        for (File file : result.getFiles()) {
            FileOutputDto item = new FileOutputDto();
            item.setId(file.getId());
            item.setName(file.getName());
            list.add(item);
        }

        return ResponseEntity.ok(list);
    }

    @PostMapping(value = {"/make-public/{fileId}"}, produces = {"application/json"})
    public ResponseEntity<Message> makePublic(@PathVariable(name = "fileId") String fileId) throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build();
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");
        drive.permissions().create(fileId, permission).execute();
        Message message = new Message();
        message.setMessage("Permission has been successfully granted.");
        return ResponseEntity.ok(message);
    }

    @DeleteMapping(value = {"/delete-file/{fileId}"}, produces = "application/json")
    public ResponseEntity<Message> deleteFile(@PathVariable(name = "fileId") String fileId) throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build();
        drive.files().delete(fileId).execute();
        Message message = new Message();
        message.setMessage("File has been deleted.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/create-folder/{folderName}")
    public ResponseEntity<Message> createFolder(@PathVariable(name = "folderName") String folder) throws Exception {
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build();
        File file = new File();
        file.setName(folder);
        file.setMimeType("application/vnd.google-apps.folder");
        drive.files().create(file).execute();
        Message message = new Message();
        message.setMessage("Folder has been created successfully.");
        return ResponseEntity.ok(message);
    }

    private Credential getCredentials()  {
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(2424).build();
        try {
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
