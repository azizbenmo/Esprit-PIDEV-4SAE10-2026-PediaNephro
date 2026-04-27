package com.pedianephro.subscription.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileRequest {
    
    @NotNull(message = "L'ID utilisateur est requis")
    private Long userId;

    @NotNull(message = "L'âge de l'enfant est requis")
    @Min(0)
    private Integer ageEnfant;

    @NotNull(message = "Le nombre de mois depuis la greffe est requis")
    @Min(0)
    private Integer moisDepuisGreffe;

    private Integer comorbidites; // 0=aucune, 1=légères, 2=sévères

    private Integer frequenceSuivi; // 1=mensuel, 2=bimensuel, 3=hebdomadaire

    private Integer aEuEpisodeRejet;

    @NotNull(message = "Le nombre d'hospitalisations est requis")
    @Min(0)
    private Integer nombreHospitalisationsAn;

    private Integer prendImmunosuppresseurs;

    @NotNull(message = "Le nombre de médicaments est requis")
    @Min(1)
    private Integer nombreMedicamentsQuotidiens;

    private Integer presenceComplicationActive;
}
