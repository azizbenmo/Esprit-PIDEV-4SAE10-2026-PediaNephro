package com.example.dossiemedicale.repositoories;
import com.example.dossiemedicale.entities.ConstantePrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConstantePredictionRepository
        extends JpaRepository<ConstantePrediction, Long> {

    List<ConstantePrediction> findByDossierIdDossierAndTypeOrderByDatePredictionAsc(
            Long idDossier,
            String type
    );
    List<ConstantePrediction> findByDossierIdDossierOrderByDatePredictionAsc(
            Long idDossier
    );
}