package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.Hopital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HopitalRepository extends JpaRepository<Hopital, Long> {
}