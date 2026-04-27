package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.*;
import com.example.dossiemedicale.entities.*;
import com.example.dossiemedicale.repositoories.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final HospitalisationRepository hospitalisationRepository;
    private final ConstanteVitaleRepository constanteVitaleRepository;
    private final ExamenRepository examenRepository;
    private final ImagerieMedicaleRepository imagerieMedicaleRepository;
    private final TemplateEngine templateEngine;

    private static final SimpleDateFormat LEGACY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat LEGACY_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateDossierMedicalPdf(Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (id=" + dossierId + ")"));

        DossierMedicalPdfDTO dto = buildPdfDto(dossier);

        Context context = new Context();
        context.setVariable("dossier", dto);

        String html = templateEngine.process("pdf/dossier-medical-complet", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private DossierMedicalPdfDTO buildPdfDto(DossierMedical dossier) {
        Enfant enfant = dossier.getEnfant();
        Patient parent = (enfant != null) ? enfant.getPatient() : null;

        List<Hospitalisation> hospitalisationsEntities = (enfant != null)
                ? hospitalisationRepository.findByEnfant_IdEnfantOrderByDateEntreeDesc(enfant.getIdEnfant())
                : Collections.emptyList();

        List<ConstanteVitale> constantesEntities =
                constanteVitaleRepository.findByDossier_IdDossierOrderByDateMesureDesc(dossier.getIdDossier());

        List<Examen> examensEntities =
                examenRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossier.getIdDossier());

        List<ImagerieMedicale> imageriesEntities =
                imagerieMedicaleRepository.findByDossier_IdDossierOrderByDateExamenDesc(dossier.getIdDossier());

        List<PdfHospitalisationDTO> hospitalisations = hospitalisationsEntities.stream()
                .map(h -> new PdfHospitalisationDTO(
                        formatDate(h.getDateEntree()),
                        formatDate(h.getDateSortie()),
                        safe(h.getMotif()),
                        safe(h.getServiceHospitalier())
                ))
                .collect(Collectors.toList());

        List<PdfConstanteVitaleDTO> constantesVitales = constantesEntities.stream()
                .map(c -> new PdfConstanteVitaleDTO(
                        safe(c.getType()),
                        c.getValeur(),
                        c.getSeuilMin(),
                        c.getSeuilMax(),
                        formatDateTime(c.getDateMesure())
                ))
                .collect(Collectors.toList());

        List<PdfExamenDTO> examens = examensEntities.stream()
                .map(e -> new PdfExamenDTO(
                        safe(e.getType()),
                        safe(e.getResultat()),
                        formatDate(e.getDateExamen())
                ))
                .collect(Collectors.toList());

        List<PdfImagerieDTO> imageries = imageriesEntities.stream()
                .map(i -> {
                    String chemin = safe(i.getCheminFichier());
                    String base64 = loadFileAsBase64(chemin);
                    String mime = guessMimeType(chemin);

                    PdfImagerieDTO dtoImg = new PdfImagerieDTO();
                    dtoImg.setType(safe(i.getType()));
                    dtoImg.setDateExamen(formatDateTime(i.getDateExamen()));
                    dtoImg.setCheminFichier(chemin);
                    dtoImg.setImageBase64(base64);
                    dtoImg.setMimeType(mime);
                    return dtoImg;
                })
                .collect(Collectors.toList());

        String qrContent = buildQrContent(dossier, enfant, parent);

        DossierMedicalPdfDTO dto = new DossierMedicalPdfDTO();
        dto.setCode(safe(dossier.getCode()));
        dto.setDateCreation(formatDate(dossier.getDateCreation()));

        dto.setEnfantNom(enfant != null ? safe(enfant.getNom()) : "");
        dto.setEnfantPrenom(enfant != null ? safe(enfant.getPrenom()) : "");
        dto.setEnfantAge(enfant != null ? enfant.getAge() : null);
        dto.setEnfantSexe(enfant != null ? safe(enfant.getSexe()) : "");
        dto.setEnfantTaille(enfant != null ? enfant.getTaille() : null);
        dto.setEnfantPoids(enfant != null ? enfant.getPoids() : null);
        dto.setEnfantDateNaissance(enfant != null ? formatDate(enfant.getDateNaissance()) : "");

        dto.setParentNom(parent != null ? safe(parent.getNom()) : "");
        dto.setParentPrenom(parent != null ? safe(parent.getPrenom()) : "");
        dto.setParentEmail(parent != null ? safe(parent.getEmail()) : "");

        dto.setHospitalisations(hospitalisations);
        dto.setConstantesVitales(constantesVitales);
        dto.setExamens(examens);
        dto.setImageries(imageries);

        dto.setLogoBase64(loadLogoBase64("static/images/logo-pedia.png"));
        dto.setQrCodeBase64(generateQrCodeBase64(qrContent));

        return dto;
    }

    private String buildQrContent(DossierMedical dossier, Enfant enfant, Patient parent) {
        StringBuilder sb = new StringBuilder();

        sb.append("Code dossier: ").append(safe(dossier.getCode())).append("\n");

        if (enfant != null) {
            sb.append("Enfant: ")
                    .append(safe(enfant.getPrenom())).append(" ")
                    .append(safe(enfant.getNom())).append("\n");
        }

        if (parent != null) {
            sb.append("Parent: ")
                    .append(safe(parent.getPrenom())).append(" ")
                    .append(safe(parent.getNom())).append("\n")
                    .append("Email: ").append(safe(parent.getEmail())).append("\n");
        }

        return sb.toString();
    }

    private String generateQrCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 220, 220);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    private String loadLogoBase64(String classpathLocation) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            try (InputStream is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String loadFileAsBase64(String dbPath) {
        try {
            if (dbPath == null || dbPath.isBlank()) return null;

            Path path = Path.of(dbPath);

            if (!Files.exists(path)) {
                System.out.println("IMAGE INTROUVABLE => " + path.toAbsolutePath());
                return null;
            }

            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String guessMimeType(String dbPath) {
        if (dbPath == null) return "image/jpeg";
        String p = dbPath.toLowerCase();
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return "image/jpeg";
        return "image/jpeg";
    }

    private String formatDate(Date date) {
        return date != null ? LEGACY_DATE_FORMAT.format(date) : "";
    }

    private String formatDateTime(Date date) {
        return date != null ? LEGACY_DATE_TIME_FORMAT.format(date) : "";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}