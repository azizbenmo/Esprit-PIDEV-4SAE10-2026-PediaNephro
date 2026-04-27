package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.ApplyPromoRequest;
import com.pedianephro.subscription.dto.ApplyPromoResponse;
import com.pedianephro.subscription.dto.PromoCodeRequest;
import com.pedianephro.subscription.dto.PromoCodeResponse;
import com.pedianephro.subscription.entity.PromoCode;
import com.pedianephro.subscription.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeResponse createPromoCode(PromoCodeRequest request) {
        String code = normalizeCode(request.getCode());
        if (promoCodeRepository.findByCode(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce code promo existe déjà.");
        }

        PromoCode p = new PromoCode();
        p.setCode(code);
        p.setDiscountPercent(request.getDiscountPercent());
        p.setMaxUses(request.getMaxUses());
        p.setCurrentUses(0);
        p.setExpiryDate(request.getExpiryDate());
        p.setActive(request.getActive() == null || request.getActive());

        PromoCode saved = promoCodeRepository.save(p);
        return toResponse(saved, true, "Code promo créé avec succès.");
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse validatePromoCode(String codeRaw) {
        String code = normalizeCode(codeRaw);
        Optional<PromoCode> optional = promoCodeRepository.findByCodeAndActiveTrue(code);
        if (optional.isEmpty()) {
            return PromoCodeResponse.builder()
                    .code(code)
                    .valid(false)
                    .message("Code promo invalide")
                    .build();
        }

        PromoCode promo = optional.get();
        return toResponse(promo, isValid(promo), validationMessage(promo));
    }

    @Transactional(readOnly = true)
    public ApplyPromoResponse applyPromoCode(ApplyPromoRequest request) {
        String code = normalizeCode(request.getCode());
        PromoCodeResponse validation = validatePromoCode(code);
        if (validation.getValid() == null || !validation.getValid()) {
            return ApplyPromoResponse.builder()
                    .valid(false)
                    .code(code)
                    .originalPrice(request.getPrice())
                    .discountedPrice(request.getPrice())
                    .savings(0.0)
                    .message(validation.getMessage())
                    .build();
        }

        double original = request.getPrice();
        double percent = validation.getDiscountPercent() == null ? 0.0 : validation.getDiscountPercent();
        double discounted = round3(original - (original * (percent / 100.0)));
        double savings = round3(original - discounted);

        return ApplyPromoResponse.builder()
                .valid(true)
                .code(code)
                .discountPercent(percent)
                .originalPrice(round3(original))
                .discountedPrice(discounted)
                .savings(savings)
                .message("Code valide")
                .build();
    }

    public PromoCode consumePromoCode(String codeRaw) {
        String code = normalizeCode(codeRaw);
        PromoCode promo = promoCodeRepository.findActiveByCodeForUpdate(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code promo invalide"));

        if (!isValid(promo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationMessage(promo));
        }

        int nextUses = (promo.getCurrentUses() == null ? 0 : promo.getCurrentUses()) + 1;
        promo.setCurrentUses(nextUses);
        if (promo.getMaxUses() != null && nextUses >= promo.getMaxUses()) {
            promo.setActive(false);
        }

        return promoCodeRepository.save(promo);
    }

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAllPromoCodes() {
        return promoCodeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PromoCode::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(p -> toResponse(p, isValid(p), validationMessage(p)))
                .collect(Collectors.toList());
    }

    public void deactivatePromoCode(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Code promo introuvable"));
        promo.setActive(false);
        promoCodeRepository.save(promo);
    }

    private boolean isValid(PromoCode promo) {
        if (promo == null) return false;
        if (!promo.isActive()) return false;
        if (promo.getExpiryDate() == null || promo.getExpiryDate().isBefore(LocalDate.now())) return false;
        if (promo.getMaxUses() == null) return false;
        int current = promo.getCurrentUses() == null ? 0 : promo.getCurrentUses();
        return current < promo.getMaxUses();
    }

    private String validationMessage(PromoCode promo) {
        if (promo == null) return "Code promo invalide";
        if (!promo.isActive()) return "Ce code promo n'est plus disponible";
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDate.now())) {
            return "Ce code promo a expiré le " + promo.getExpiryDate();
        }
        if (promo.getMaxUses() != null) {
            int current = promo.getCurrentUses() == null ? 0 : promo.getCurrentUses();
            if (current >= promo.getMaxUses()) return "Ce code promo a atteint sa limite d'utilisation";
        }
        return "Code valide";
    }

    private PromoCodeResponse toResponse(PromoCode p, boolean valid, String message) {
        return PromoCodeResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .discountPercent(p.getDiscountPercent())
                .maxUses(p.getMaxUses())
                .currentUses(p.getCurrentUses())
                .expiryDate(p.getExpiryDate())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .valid(valid)
                .message(message)
                .build();
    }

    private String normalizeCode(String code) {
        if (code == null) return "";
        return code.trim().toUpperCase();
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}

