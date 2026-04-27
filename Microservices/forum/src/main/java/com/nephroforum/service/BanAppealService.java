package com.nephroforum.service;

import com.nephroforum.entity.BanAppeal;
import com.nephroforum.entity.BanAppeal.AppealStatus;
import com.nephroforum.exception.BadRequestException;
import com.nephroforum.repository.BanAppealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BanAppealService {

    private final BanAppealRepository appealRepo;
    private final BanService banService;

    @Transactional
    public BanAppeal createAppeal(String username, String reason) {
        // Un seul appel en attente à la fois
        if (appealRepo.existsByUsernameAndStatus(username, AppealStatus.PENDING)) {
            throw new BadRequestException("Vous avez déjà une réclamation en attente.");
        }

        return appealRepo.save(BanAppeal.builder()
                .username(username)
                .reason(reason)
                .build());
    }

    public List<BanAppeal> getPendingAppeals() {
        return appealRepo.findByStatusOrderByCreatedAtDesc(AppealStatus.PENDING);
    }

    public List<BanAppeal> getAllAppeals() {
        return appealRepo.findByStatusOrderByCreatedAtDesc(AppealStatus.PENDING);
    }

    @Transactional
    public void acceptAppeal(Long appealId) {
        BanAppeal appeal = appealRepo.findById(appealId)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));
        appeal.setStatus(AppealStatus.ACCEPTED);
        appealRepo.save(appeal);
        // Lève le ban automatiquement
        banService.resetBan(appeal.getUsername());
    }

    @Transactional
    public void rejectAppeal(Long appealId) {
        BanAppeal appeal = appealRepo.findById(appealId)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));
        appeal.setStatus(AppealStatus.REJECTED);
        appealRepo.save(appeal);
    }
}