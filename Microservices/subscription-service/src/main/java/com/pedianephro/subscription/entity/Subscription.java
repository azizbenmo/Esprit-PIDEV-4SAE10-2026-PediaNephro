package com.pedianephro.subscription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription", indexes = {
    @Index(name = "idx_subscription_user_id", columnList = "user_id"),
    @Index(name = "idx_subscription_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 150)
    @Column(name = "user_full_name", length = 150)
    private String userFullName;

    @NotBlank
    @Email
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Size(max = 50)
    @Column(name = "promo_code_used", length = 50)
    private String promoCodeUsed;

    @Column(name = "discounted_monthly_price")
    private Double discountedMonthlyPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        normalizeUserEmail();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeUserEmail();
        updatedAt = LocalDateTime.now();
    }

    private void normalizeUserEmail() {
        String email = userEmail;
        if (email != null) {
            email = email.trim();
        }
        if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            userEmail = "user" + (userId != null ? userId : "0") + "@pedianephro.com";
        } else {
            userEmail = email;
        }
    }
}
