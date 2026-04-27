package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenResumeItem {
    private Long idExamen;
    private String type;
    private String resultat;
    private Date dateExamen;
}