package com.example.dossiemedicale.repositoories;

import com.example.dossiemedicale.entities.ConstanteVitale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ConstanteVitaleRepository extends JpaRepository<ConstanteVitale, Long> {

    List<ConstanteVitale> findByDossierIdDossierAndTypeOrderByDateMesureAsc(
            Long idDossier,
            String type
    );

    List<ConstanteVitale> findTop2ByDossierIdDossierAndTypeOrderByDateMesureDesc(
            Long dossierId,
            String type
    );

    @Query("select d.enfant.patient.email from DossierMedical d where d.idDossier = :dossierId")
    String findPatientEmailByDossierId(@Param("dossierId") Long dossierId);

    // la constante précédente (avant la nouvelle), même dossier + type, tri desc
    ConstanteVitale findTop1ByDossierIdDossierAndTypeAndIdConstanteNotOrderByDateMesureDesc(
            Long dossierId,
            String type,
            Long idConstante
    );

    @Query("""
        select cv
        from ConstanteVitale cv
        where cv.dossier.idDossier = :dossierId
          and cv.type = :type
          and cv.idConstante < :currentId
        order by cv.idConstante desc
    """)
    List<ConstanteVitale> findPreviousById(@Param("dossierId") Long dossierId,
                                           @Param("type") String type,
                                           @Param("currentId") Long currentId);

    List<ConstanteVitale> findByDossier_IdDossierOrderByDateMesureDesc(Long dossierId);

    List<ConstanteVitale> findTop5ByDossier_IdDossierOrderByDateMesureDesc(Long dossierId);
    List<ConstanteVitale> findTop20ByDossier_IdDossierOrderByDateMesureDesc(Long dossierId);


    List<ConstanteVitale> findByDossier_Enfant_IdEnfantAndDateMesureBefore(Long enfantId, Date date);

    List<ConstanteVitale> findByDossier_Enfant_IdEnfantAndDateMesureAfter(Long enfantId, Date date);


}