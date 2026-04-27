package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.RapportConsultationExterne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RapportConsultationExterneRepository extends JpaRepository<RapportConsultationExterne, Long> {
    Optional<RapportConsultationExterne> findByConsultationId(Long consultationId);

    List<RapportConsultationExterne> findByDossier_IdDossierOrderByDateRapportDescIdDesc(Long dossierId);
}
