package com.pedianephro.subscription.repository;

import com.pedianephro.subscription.entity.Subscription;
import com.pedianephro.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT DISTINCT s FROM Subscription s JOIN FETCH s.plan p LEFT JOIN FETCH p.features WHERE s.userId = :userId")
    List<Subscription> findByUserId(@Param("userId") Long userId);

    Subscription findTopByUserIdAndStatusOrderByEndDateDesc(Long userId, SubscriptionStatus status);

    List<Subscription> findByPlan_Id(Long planId);

    long countByPlan_Id(Long planId);

    List<Subscription> findByStatusAndEndDateBetweenAndReminderSentFalse(
            SubscriptionStatus status, LocalDate start, LocalDate end);

    List<Subscription> findByStatusAndEndDateBetweenAndAutoRenewTrue(
            SubscriptionStatus status, LocalDate start, LocalDate end);

    List<Subscription> findByStatusAndEndDateBeforeAndAutoRenewTrue(
            SubscriptionStatus status, LocalDate date);
}
