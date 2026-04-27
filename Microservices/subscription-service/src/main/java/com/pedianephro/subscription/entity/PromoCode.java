package com.pedianephro.subscription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_code", indexes = {
        @Index(name = "idx_promo_code_code", columnList = "code"),
        @Index(name = "idx_promo_code_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;

    @NotNull
    @Positive
    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @NotNull
    @Min(0)
    @Column(name = "current_uses", nullable = false)
    private Integer currentUses;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (currentUses == null) {
            currentUses = 0;
        }
    }
}

