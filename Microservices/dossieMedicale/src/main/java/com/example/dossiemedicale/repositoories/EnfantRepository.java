package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Enfant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnfantRepository extends JpaRepository<Enfant, Long> {

    List<Enfant> findByPatient_IdPatient(Long patientId);
}