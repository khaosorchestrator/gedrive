package com.ldsa.gedrive.controllers;

import com.ldsa.gedrive.dtos.GoogleDriveFileDTO;
import com.ldsa.gedrive.services.GoogleDriveFileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Files API")
public class GoogleDriveFileController {

    private final GoogleDriveFileService googleDriveFileService;

    @GetMapping
    public ResponseEntity<List<GoogleDriveFileDTO>> findAll() {
        return ResponseEntity.ok(googleDriveFileService.findAll());
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<List<GoogleDriveFileDTO>> findAllInFolder(@PathVariable String folderId) {
        return ResponseEntity.ok(googleDriveFileService.findAllInFolder(folderId));
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String path,
            @RequestParam("shared") String shared) {
        path = "".equals(path) ?"Root" : path;
        return googleDriveFileService.upload(file, path, Boolean.parseBoolean(shared));
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        googleDriveFileService.deleteById(id);
    }

    @GetMapping("/download/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void download(@PathVariable String id, HttpServletResponse response) throws IOException {
        googleDriveFileService.download(id, response.getOutputStream());
    }

    @GetMapping("/{fileId}/copy/{folderName}")
    @ResponseStatus(HttpStatus.OK)
    public void copy(@PathVariable String fileId, @PathVariable String folderName) {
        googleDriveFileService.copyToFolder(fileId, folderName);
    }

    @GetMapping("/{fileId}/move/{folderName}")
    @ResponseStatus(HttpStatus.OK)
    public void move(@PathVariable String fileId, @PathVariable String folderName) {
        googleDriveFileService.moveToFolder(fileId, folderName);
    }

    @PostMapping("/{fileId}/permission/{gmail}")
    @ResponseStatus(HttpStatus.OK)
    public void permission(@PathVariable String fileId, @PathVariable String gmail) {
        googleDriveFileService.shareFile(fileId, gmail);
    }
}
