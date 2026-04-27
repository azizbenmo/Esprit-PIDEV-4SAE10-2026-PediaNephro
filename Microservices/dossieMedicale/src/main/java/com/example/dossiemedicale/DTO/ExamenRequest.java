package com.example.dossiemedicale.DTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ExamenRequest {
    private String type;
    private String resultat;
    private Date dateExamen;
    private Long dossierId;
}