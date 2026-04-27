package com.example.dossiemedicale.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlerteRequest {
    private String niveau;
    private String message;
    private Date dateDeclenchement;
    private Long constanteId;
}
