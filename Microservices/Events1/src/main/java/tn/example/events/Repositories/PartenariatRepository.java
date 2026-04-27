package tn.example.events.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.example.events.Entities.Partenariat;
import tn.example.events.Entities.StatutPartenariat;
import java.util.List;

public interface PartenariatRepository extends JpaRepository<Partenariat, Long> {
    List<Partenariat> findByStatut(StatutPartenariat statut);

    //List<Partenariat> findByArchiveFalse();
    //List<Partenariat> findByArchiveTrue();
    //List<Partenariat> findByStatutAndArchiveFalse(StatutPartenariat statut);
}