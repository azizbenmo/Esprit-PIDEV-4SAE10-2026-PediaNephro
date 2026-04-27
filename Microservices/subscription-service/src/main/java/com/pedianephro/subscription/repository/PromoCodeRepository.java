package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCodeAndActiveTrue(String code);

    Optional<PromoCode> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PromoCode p where p.code = :code and p.active = true")
    Optional<PromoCode> findActiveByCodeForUpdate(@Param("code") String code);
}
