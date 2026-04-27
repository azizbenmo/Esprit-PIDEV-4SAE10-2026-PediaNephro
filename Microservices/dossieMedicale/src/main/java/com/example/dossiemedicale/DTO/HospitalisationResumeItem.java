package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalisationResumeItem {
    private Long idHospitalisation;
    private Date dateEntree;
    private Date dateSortie;
    private String motif;
    private String serviceHospitalier;
}