package com.example.consultation_microservice.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/apiConsultation/fichiers")
public class FileController {

    /**
     * ✅ Sert un fichier uploadé par son nom.
     * Ex: GET /apiConsultation/fichiers/radiographie.jpeg
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> servirFichier(@PathVariable String filename) {
        try {
            // ✅ Extraire uniquement le nom du fichier depuis un chemin absolu Windows ou Unix
            // Remplacer les backslashes Windows par des slashes, puis prendre la dernière partie
            String nomSeul = filename.replace("\\", "/");
            if (nomSeul.contains("/")) {
                nomSeul = nomSeul.substring(nomSeul.lastIndexOf("/") + 1);
            }

            Path filePath = Paths.get("uploads").resolve(nomSeul).normalize();
            File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // Détecter automatiquement le type MIME
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nomSeul + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}