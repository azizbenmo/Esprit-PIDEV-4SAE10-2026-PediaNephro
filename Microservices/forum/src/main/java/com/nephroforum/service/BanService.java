package com.nephroforum.service;

import com.nephroforum.entity.Ban;
import com.nephroforum.repository.BanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BanService {

    private static final int MAX_VIOLATIONS = 3;
    private static final int BAN_HOURS = 24;

    private final BanRepository banRepo;

    // ── Vérifie si l'user est banni ───────────────────────────────────────────
    public boolean isBanned(String username) {
        return banRepo.findByUsername(username).map(ban -> {
            if (ban.isActive() && ban.getBannedUntil() != null) {
                if (LocalDateTime.now().isBefore(ban.getBannedUntil())) {
                    return true;
                } else {
                    // Ban expiré — reset
                    ban.setActive(false);
                    banRepo.save(ban);
                    return false;
                }
            }
            return false;
        }).orElse(false);
    }

    // ── Retourne le statut du ban ─────────────────────────────────────────────
    public Map<String, Object> getBanStatus(String username) {
        return banRepo.findByUsername(username).map(ban -> {
            boolean banned = ban.isActive() &&
                    ban.getBannedUntil() != null &&
                    LocalDateTime.now().isBefore(ban.getBannedUntil());

            long minutesLeft = 0;
            if (banned) {
                minutesLeft = java.time.Duration.between(
                        LocalDateTime.now(), ban.getBannedUntil()).toMinutes();
            }

            return Map.<String, Object>of(
                    "banned", banned,
                    "violationCount", ban.getViolationCount(),
                    "minutesLeft", minutesLeft,
                    "bannedUntil", ban.getBannedUntil() != null ?
                            ban.getBannedUntil().toString() : "",
                    "warningsLeft", Math.max(0, MAX_VIOLATIONS - ban.getViolationCount())
            );
        }).orElse(Map.of(
                "banned", false,
                "violationCount", 0,
                "minutesLeft", 0,
                "bannedUntil", "",
                "warningsLeft", MAX_VIOLATIONS
        ));
    }

    // ── Ajoute une violation ──────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> addViolation(String username) {
        Ban ban = banRepo.findByUsername(username).orElseGet(() ->
                Ban.builder()
                        .username(username)
                        .violationCount(0)
                        .active(false)
                        .build()
        );

        ban.setViolationCount(ban.getViolationCount() + 1);
        ban.setUpdatedAt(LocalDateTime.now());

        if (ban.getViolationCount() >= MAX_VIOLATIONS) {
            ban.setActive(true);
            ban.setBannedUntil(LocalDateTime.now().plusHours(BAN_HOURS));
            banRepo.save(ban);

            return Map.of(
                    "banned", true,
                    "violationCount", ban.getViolationCount(),
                    "message", "🚫 Vous avez été banni pour 24h suite à 3 violations.",
                    "bannedUntil", ban.getBannedUntil().toString(),
                    "minutesLeft", (long) (BAN_HOURS * 60),
                    "warningsLeft", 0
            );
        }

        banRepo.save(ban);
        int warningsLeft = MAX_VIOLATIONS - ban.getViolationCount();

        return Map.of(
                "banned", false,
                "violationCount", ban.getViolationCount(),
                "message", String.format(
                        "⚠️ Avertissement %d/3 — Encore %d violation(s) avant d'être banni 24h.",
                        ban.getViolationCount(), warningsLeft),
                "bannedUntil", "",
                "minutesLeft", 0L,
                "warningsLeft", warningsLeft
        );
    }

    // ── Reset violations (admin) ──────────────────────────────────────────────
    @Transactional
    public void resetBan(String username) {
        banRepo.findByUsername(username).ifPresent(ban -> {
            ban.setActive(false);
            ban.setViolationCount(0);
            ban.setBannedUntil(null);
            ban.setUpdatedAt(LocalDateTime.now());
            banRepo.save(ban);
        });
    }

    @Transactional
    public Map<String, Object> adminBan(String username) {
        Ban ban = banRepo.findByUsername(username).orElseGet(() ->
                Ban.builder()
                        .username(username)
                        .violationCount(3)
                        .active(false)
                        .build()
        );
        ban.setActive(true);
        ban.setViolationCount(Math.max(ban.getViolationCount(), 3));
        ban.setBannedUntil(LocalDateTime.now().plusHours(BAN_HOURS));
        ban.setUpdatedAt(LocalDateTime.now());
        banRepo.save(ban);
        return Map.of(
                "banned", true,
                "message", username + " banni 24h par l'admin",
                "bannedUntil", ban.getBannedUntil().toString()
        );
    }
}