package com.example.dossiemedicale.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfImagerieDTO {
    private String type;
    private String dateExamen;

    // Chemin qui vient de la DB (peut être "C:\..\img.jpg" ou "/home/..../img.png")
    private String cheminFichier;

    // NEW pour afficher l'image dans le PDF
    private String imageBase64;
    private String mimeType;
}