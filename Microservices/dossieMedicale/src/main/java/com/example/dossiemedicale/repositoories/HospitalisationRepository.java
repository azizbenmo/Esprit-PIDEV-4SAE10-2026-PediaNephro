package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Hospitalisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalisationRepository extends JpaRepository<Hospitalisation, Long> {


    List<Hospitalisation> findByEnfant_IdEnfantOrderByDateEntreeDesc(Long enfantId);

    List<Hospitalisation> findTop5ByEnfant_IdEnfantOrderByDateEntreeDesc(Long enfantId);

    List<Hospitalisation> findTop20ByEnfant_IdEnfantOrderByDateEntreeDesc(Long enfantId);
}