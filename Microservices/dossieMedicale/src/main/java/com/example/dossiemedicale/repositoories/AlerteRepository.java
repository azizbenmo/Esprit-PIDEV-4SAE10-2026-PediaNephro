package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    boolean existsByConstante_IdConstante(Long idConstante);


    List<Alerte> findByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(Long dossierId);


    List<Alerte> findTop5ByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(Long dossierId);

    List<Alerte> findTop20ByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(Long dossierId);


    List<Alerte> findByConstante_Dossier_IdDossierAndDateDeclenchementBefore(Long dossierId, Date date);

    List<Alerte> findByConstante_Dossier_IdDossierAndDateDeclenchementAfter(Long dossierId, Date date);
}
