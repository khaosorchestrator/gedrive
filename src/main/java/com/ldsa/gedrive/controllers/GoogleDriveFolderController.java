package com.ldsa.gedrive.controllers;

import com.ldsa.gedrive.dtos.GoogleDriveFolderDTO;
import com.ldsa.gedrive.services.GoogleDriveFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class GoogleDriveFolderController {

    private final GoogleDriveFolderService googleDriveFolderService;

    @GetMapping
    public ResponseEntity<List<GoogleDriveFolderDTO>> findAll() {
        return ResponseEntity.ok(googleDriveFolderService.findAll());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestParam("folderName") String folderName) {
        googleDriveFolderService.create(folderName);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        googleDriveFolderService.delete(id);
    }
}
