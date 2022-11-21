package com.ldsa.googledriverestapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
public class HomeController {

    private static final NetHttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_NAME = "google-drive-rest-api";

    private static final String USER_IDENTIFIER_KEY = "";

    @Value("${google.oauth.uri}")
    private String OAUTH_URI;


    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws Exception {
        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(Objects.requireNonNull(HomeController.class.getResourceAsStream(CREDENTIALS_FILE_PATH))));
        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FILE_PATH))).build();
    }

    @GetMapping
    public String home() throws Exception {
        boolean isUserAuthenticated = false;

        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
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
        String redirectURL = url.setRedirectUri(OAUTH_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    @GetMapping("/oauth")
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code);
            return "dashboard.html";
        }
        return "index.html";
    }

    private void saveToken(String code) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(OAUTH_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
    }

    @GetMapping("/create")
    public void createFile(HttpServletResponse response) throws Exception {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);

        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
                .setApplicationName(APPLICATION_NAME).build();

        File file = new File();
        file.setName("profile.jpg");

        FileContent content = new FileContent("image/jpeg", new java.io.File("D:\\practice\\sbtgd\\sample.jpg"));
        File uploadedFile = drive.files().create(file, content).setFields("id").execute();

        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
        response.getWriter().write(fileReference);
    }

    @PostMapping("/upload-in-folder")
    public void uploadFileInFolder(HttpServletResponse response, MultipartFile requestFile) throws Exception {
        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName("google-driver-rest-api").build();

        File file = new File();
        file.setName("digit.jpg");
        file.setParents(List.of("1_TsS7arQRBMY2t4NYKNdxta8Ty9r6wva"));

        FileContent content = new FileContent("image/jpeg", new java.io.File(""));
        File uploadedFile = drive.files().create(file, content).setFields("id").execute();

        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
        response.getWriter().write(fileReference);
    }

    @GetMapping("/list-files")
    public ResponseEntity<List<FileOutputDto>> listFiles() throws Exception {
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        List<FileOutputDto> list = new ArrayList<>();

        FileList fileList = drive.files().list().setFields("files(id,name)").execute();

        for (File file : fileList.getFiles()) {
            FileOutputDto item = new FileOutputDto();
            item.setId(file.getId());
            item.setName(file.getName());
            list.add(item);
        }

        return ResponseEntity.ok(list);
    }

    @PostMapping(value = {"/make-public/{fileId}"}, produces = {"application/json"})
    public ResponseEntity<Message> makePublic(@PathVariable(name = "fileId") String fileId) throws Exception {
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("google-driver-rest-api").build();
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
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("google-driver-rest-api").build();
        drive.files().delete(fileId).execute();
        Message message = new Message();
        message.setMessage("File has been deleted.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/create-folder/{folderName}")
    public ResponseEntity<Message> createFolder(@PathVariable(name = "folderName") String folder) throws Exception {
        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("google-driver-rest-api").build();
        File file = new File();
        file.setName(folder);
        file.setMimeType("application/vnd.google-apps.folder");
        drive.files().create(file).execute();
        Message message = new Message();
        message.setMessage("Folder has been created successfully.");
        return ResponseEntity.ok(message);
    }
}
