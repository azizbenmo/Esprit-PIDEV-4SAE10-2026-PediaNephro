package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    List<Examen> findByDossier_IdDossierOrderByDateExamenDesc(Long dossierId);


    List<Examen> findTop5ByDossier_IdDossierOrderByDateExamenDesc(Long dossierId);
    List<Examen> findTop20ByDossier_IdDossierOrderByDateExamenDesc(Long dossierId);
}
