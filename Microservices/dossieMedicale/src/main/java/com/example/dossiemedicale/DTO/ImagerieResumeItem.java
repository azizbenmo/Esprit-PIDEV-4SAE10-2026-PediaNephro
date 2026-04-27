package com.example.dossiemedicale.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagerieResumeItem {
    private Long idImagerie;
    private String type;
    private String description;
    private Date dateExamen;
    private String cheminFichier;
}