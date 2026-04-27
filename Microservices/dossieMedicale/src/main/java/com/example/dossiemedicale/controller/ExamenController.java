package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.DTO.ExamenRequest;
import com.example.dossiemedicale.entities.Examen;
import com.example.dossiemedicale.services.ExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/examens")
@RequiredArgsConstructor
public class ExamenController {

    private final ExamenService examenService;

    @PostMapping(value = "/ajouter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Examen> ajouterExamen(
            @RequestParam("type") String type,
            @RequestParam("resultat") String resultat,
            @RequestParam("dateExamen") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateExamen,
            @RequestParam("dossierId") Long dossierId,
            @RequestParam(value = "fichier", required = false) MultipartFile fichier
    ) throws IOException {

        ExamenRequest request = new ExamenRequest();
        request.setType(type);
        request.setResultat(resultat);
        request.setDateExamen(dateExamen);
        request.setDossierId(dossierId);

        Examen saved = examenService.ajouterExamen(request, fichier);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping(value = "/modifier/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Examen> modifierExamen(
            @PathVariable Long id,
            @RequestParam("type") String type,
            @RequestParam("resultat") String resultat,
            @RequestParam("dateExamen") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateExamen,
            @RequestParam("dossierId") Long dossierId,
            @RequestParam(value = "fichier", required = false) MultipartFile fichier
    ) throws IOException {

        ExamenRequest request = new ExamenRequest();
        request.setType(type);
        request.setResultat(resultat);
        request.setDateExamen(dateExamen);
        request.setDossierId(dossierId);

        return ResponseEntity.ok(examenService.modifierExamen(id, request, fichier));
    }

    @DeleteMapping("/supprimer/{id}")
    public ResponseEntity<Void> supprimerExamen(@PathVariable Long id) {
        examenService.supprimerExamen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Examen> getExamenById(@PathVariable Long id) {
        return ResponseEntity.ok(examenService.getExamenById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Examen>> getAllExamens() {
        return ResponseEntity.ok(examenService.getAllExamens());
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) {
        Examen examen = examenService.getExamenById(id);

        if (examen.getFichierExamen() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + examen.getNomFichier() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(examen.getFichierExamen());
    }
}