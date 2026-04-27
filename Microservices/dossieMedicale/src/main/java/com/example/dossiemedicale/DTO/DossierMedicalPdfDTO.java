package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DossierMedicalPdfDTO {

    private String code;
    private String dateCreation;

    private String enfantNom;
    private String enfantPrenom;
    private Integer enfantAge;
    private String enfantSexe;
    private Double enfantTaille;
    private Double enfantPoids;
    private String enfantDateNaissance;

    private String parentNom;
    private String parentPrenom;
    private String parentEmail;

    private String logoBase64;
    private String qrCodeBase64;

    private List<PdfHospitalisationDTO> hospitalisations;
    private List<PdfConstanteVitaleDTO> constantesVitales;
    private List<PdfExamenDTO> examens;
    private List<PdfImagerieDTO> imageries;
}