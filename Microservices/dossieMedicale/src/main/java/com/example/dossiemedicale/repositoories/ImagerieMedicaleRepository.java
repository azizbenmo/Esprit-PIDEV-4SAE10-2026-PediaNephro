package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.ImagerieMedicale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagerieMedicaleRepository extends JpaRepository<ImagerieMedicale, Long> {

    Optional<ImagerieMedicale> findByCheminFichier(String cheminFichier);
    List<ImagerieMedicale> findByDossier_IdDossierOrderByDateExamenDesc(Long dossierId);


    List<ImagerieMedicale> findTop5ByDossier_IdDossierOrderByDateExamenDesc(Long dossierId);

}


