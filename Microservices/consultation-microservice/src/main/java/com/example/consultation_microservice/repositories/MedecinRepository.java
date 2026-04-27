package com.example.consultation_microservice.repositories;

import com.example.consultation_microservice.entities.ConsultationStatus;
import com.example.consultation_microservice.entities.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    List<Medecin> findBySpecialiteAndDisponible(String specialite, Boolean disponible);

    Optional<Medecin> findByUserId(Long userId);

    Optional<Medecin> findFirstByEmailIgnoreCase(String email);
}
