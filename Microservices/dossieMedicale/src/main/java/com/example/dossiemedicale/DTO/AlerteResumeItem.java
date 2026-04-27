package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteResumeItem {
    private Long idAlerte;
    private String niveau;
    private String message;
    private Date dateDeclenchement;
    private String typeConstante;
}