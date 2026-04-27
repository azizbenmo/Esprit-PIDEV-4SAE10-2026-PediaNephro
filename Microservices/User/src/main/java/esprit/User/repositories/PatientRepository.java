package esprit.User.repositories;

import esprit.User.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByParent_Id(Long parentId);

    List<Patient> findByDoctor_Id(Long doctorId);

    long countByParent_Id(Long parentId);
}


