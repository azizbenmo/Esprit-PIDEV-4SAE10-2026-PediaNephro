package com.example.consultation_microservice.controllers;

import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apiConsultation/patient")
public class PatientController {
    @Autowired
    private ConsultationRepository consultationRepository;

    @GetMapping("/historiquepatient/{patientId}")
    public List<Consultation> getHistoriquePatient(@PathVariable Long patientId) {
        return consultationRepository.findConsultationsForPatientReference(patientId);
    }

    @GetMapping("/historique-parent")
    public List<Consultation> getHistoriqueParent(@RequestParam("email") String email) {
        if (email == null || email.trim().isBlank()) {
            return List.of();
        }
        return consultationRepository.findByDemandeurParentEmailIgnoreCaseOrderByDateSouhaiteeAsc(email.trim());
    }
}
