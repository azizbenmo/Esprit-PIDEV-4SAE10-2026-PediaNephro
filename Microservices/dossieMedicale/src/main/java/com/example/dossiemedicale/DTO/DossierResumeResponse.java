package com.example.dossiemedicale.DTO;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierResumeResponse {

    private Long idDossier;
    private String code;
    private LocalDate dateCreation;

    private Long idEnfant;
    private String nomEnfant;
    private String prenomEnfant;
    private Integer ageEnfant;
    private String sexeEnfant;
    private Double tailleEnfant;
    private Double poidsEnfant;

    private Long idPatient;
    private String nomPatient;
    private String prenomPatient;
    private String emailPatient;

    private List<ConstanteResumeItem> dernieresConstantes;
    private List<AlerteResumeItem> dernieresAlertes;
    private List<ExamenResumeItem> derniersExamens;
    private List<ImagerieResumeItem> dernieresImageries;
    private List<HospitalisationResumeItem> dernieresHospitalisations;

    private String resumeGlobal;
}