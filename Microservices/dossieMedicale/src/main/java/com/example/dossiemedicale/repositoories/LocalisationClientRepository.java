package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.LocalisationClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalisationClientRepository extends JpaRepository<LocalisationClient, Long> {

    Optional<LocalisationClient> findTopByEnfant_IdEnfantOrderByHorodatageDesc(Long enfantId);
}