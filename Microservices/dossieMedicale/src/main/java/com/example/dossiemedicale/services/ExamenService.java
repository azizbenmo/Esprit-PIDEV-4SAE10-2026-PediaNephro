package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.ExamenRequest;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.Examen;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.ExamenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final DossierMedicalRepository dossierMedicalRepository;

    public Examen ajouterExamen(ExamenRequest request, MultipartFile fichier) throws IOException {

        DossierMedical dossier = dossierMedicalRepository.findById(request.getDossierId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dossier introuvable (id=" + request.getDossierId() + ")"
                ));

        Examen examen = new Examen();
        examen.setType(request.getType());
        examen.setResultat(request.getResultat());
        examen.setDateExamen(request.getDateExamen());
        examen.setDossier(dossier);

        if (fichier != null && !fichier.isEmpty()) {
            if (!"application/pdf".equals(fichier.getContentType())) {
                throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés");
            }

            examen.setNomFichier(fichier.getOriginalFilename());
            examen.setTypeFichier(fichier.getContentType());
            examen.setFichierExamen(fichier.getBytes());
        }

        return examenRepository.save(examen);
    }

    public Examen modifierExamen(Long id, ExamenRequest request, MultipartFile fichier) throws IOException {

        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable (id=" + id + ")"));

        examen.setType(request.getType());
        examen.setResultat(request.getResultat());
        examen.setDateExamen(request.getDateExamen());

        if (request.getDossierId() != null) {
            DossierMedical dossier = dossierMedicalRepository.findById(request.getDossierId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Dossier introuvable (id=" + request.getDossierId() + ")"
                    ));
            examen.setDossier(dossier);
        }

        if (fichier != null && !fichier.isEmpty()) {
            if (!"application/pdf".equals(fichier.getContentType())) {
                throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés");
            }

            examen.setNomFichier(fichier.getOriginalFilename());
            examen.setTypeFichier(fichier.getContentType());
            examen.setFichierExamen(fichier.getBytes());
        }

        return examenRepository.save(examen);
    }

    public void supprimerExamen(Long id) {
        examenRepository.deleteById(id);
    }

    public Examen getExamenById(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable (id=" + id + ")"));
    }

    public List<Examen> getAllExamens() {
        return examenRepository.findAll();
    }
}