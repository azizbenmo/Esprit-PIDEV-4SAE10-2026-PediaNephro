package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.entities.ImagerieMedicale;
import com.example.dossiemedicale.services.ImagerieMedicaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/imageries-medicales")
@RequiredArgsConstructor
public class ImagerieMedicaleController {

    private final ImagerieMedicaleService service;

    @PostMapping("/analyser/{dossierId}")
    public ResponseEntity<ImagerieMedicale> analyser(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long dossierId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.analyserEtSauvegarder(file, dossierId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ImagerieMedicale>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagerieMedicale> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modifier/{id}")
    public ResponseEntity<ImagerieMedicale> update(
            @PathVariable Long id,
            @RequestBody ImagerieMedicale imagerie) {
        return ResponseEntity.ok(service.update(id, imagerie));
    }
}