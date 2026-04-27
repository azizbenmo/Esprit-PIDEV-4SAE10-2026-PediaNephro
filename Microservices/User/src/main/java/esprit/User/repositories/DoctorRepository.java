package esprit.User.repositories;

import esprit.User.entities.Doctor;
import esprit.User.entities.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByUser_Id(Long userId);

    Optional<Doctor> findByUserUsername(String username);

    Page<Doctor> findByStatus(DoctorStatus status, Pageable pageable);

    List<Doctor> findByStatusOrderByYearsOfExperienceDesc(DoctorStatus status);
}


