package com.ldsa.googledriverestapi.controllers;

import com.ldsa.googledriverestapi.dtos.GoogleDriveFolderDTO;
import com.ldsa.googledriverestapi.services.GoogleDriveFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class GoogleDriveFolderController {

    private final GoogleDriveFolderService service;

    @GetMapping
    public ResponseEntity<List<GoogleDriveFolderDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestParam("folderName") String folderName) {
        service.create(folderName);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
