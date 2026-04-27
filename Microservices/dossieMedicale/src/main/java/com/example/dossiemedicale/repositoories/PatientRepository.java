package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmailIgnoreCase(String email);
}
