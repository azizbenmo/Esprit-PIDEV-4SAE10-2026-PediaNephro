package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.RecommandationSuivi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommandationSuiviRepository extends JpaRepository<RecommandationSuivi, Long> {

    List<RecommandationSuivi> findByDossierIdOrderByDateCreationDesc(Long dossierId);
}