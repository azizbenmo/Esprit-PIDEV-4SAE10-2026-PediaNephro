package com.example.dossiemedicale.services;

import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.ImagerieMedicale;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.ImagerieMedicaleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ImagerieMedicaleService {

    @Autowired
    private ImagerieMedicaleRepository repository;

    @Autowired
    private DossierMedicalRepository dossierRepository;

    @Autowired
    private RestTemplate restTemplate;


    public ImagerieMedicale analyserEtSauvegarder(MultipartFile file, Long dossierId) {

        String fastApiUrl = "http://localhost:8000/prediction_type_fichier";

        File tempFile = null;

        try {

            DossierMedical dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

            // Créer fichier temporaire
            tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 👇 On récupère directement un Map
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            String type = responseBody.get("type").toString();
            Double confidence = Double.valueOf(responseBody.get("confidence").toString());

            ImagerieMedicale imagerie = new ImagerieMedicale();
            imagerie.setCheminFichier(file.getOriginalFilename());
            imagerie.setType(type);
            imagerie.setDescription(" TEST  " + confidence);
            imagerie.setDateExamen(new Date());
            imagerie.setDossier(dossier);

            log.info("Imagerie ajoutée avec IA - Type détecté : {}", type);

            return repository.save(imagerie);

        }
        catch (HttpClientErrorException e) {

            //  Si FastAPI retourne 400
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Image invalide : " + e.getResponseBodyAsString()
            );
        }
        catch (Exception e) {

            log.error("Erreur appel FastAPI", e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur communication IA"
            );
        }
        finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }


    public List<ImagerieMedicale> getAll() {
        return repository.findAll();
    }


    public ImagerieMedicale getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagerie non trouvée"));
    }



    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Imagerie non trouvée");
        }
        repository.deleteById(id);
        log.info("Imagerie supprimée avec ID : {}", id);
    }


    public ImagerieMedicale update(Long id, ImagerieMedicale newData) {

        ImagerieMedicale imagerie = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagerie non trouvée"));

        imagerie.setType(newData.getType());
        imagerie.setDescription(newData.getDescription());
        imagerie.setDateExamen(newData.getDateExamen());
        imagerie.setCheminFichier(newData.getCheminFichier());

        return repository.save(imagerie);
    }
}
