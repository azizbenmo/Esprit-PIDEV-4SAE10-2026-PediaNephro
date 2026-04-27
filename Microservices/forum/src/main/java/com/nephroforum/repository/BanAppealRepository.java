package com.nephroforum.repository;

import com.nephroforum.entity.BanAppeal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BanAppealRepository extends JpaRepository<BanAppeal, Long> {
    List<BanAppeal> findByUsernameOrderByCreatedAtDesc(String username);
    List<BanAppeal> findByStatusOrderByCreatedAtDesc(BanAppeal.AppealStatus status);
    boolean existsByUsernameAndStatus(String username, BanAppeal.AppealStatus status);
}