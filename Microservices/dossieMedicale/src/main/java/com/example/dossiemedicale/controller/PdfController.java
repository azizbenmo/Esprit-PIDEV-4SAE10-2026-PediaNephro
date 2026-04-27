package com.example.dossiemedicale.controller;

import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.services.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final DossierMedicalRepository dossierMedicalRepository;

    @GetMapping("/dossiers-medicaux/{id}")
    public ResponseEntity<byte[]> downloadDossierMedicalPdf(@PathVariable Long id) {
        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (id=" + id + ")"));

        byte[] pdf = pdfService.generateDossierMedicalPdf(id);

        String fileName = "dossier-medical-" +
                (dossier.getCode() != null ? dossier.getCode() : id) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(fileName).build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}