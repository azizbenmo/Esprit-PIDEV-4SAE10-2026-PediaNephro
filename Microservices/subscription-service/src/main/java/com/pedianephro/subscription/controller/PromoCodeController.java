package com.pedianephro.subscription.controller;

import com.pedianephro.subscription.dto.ApplyPromoRequest;
import com.pedianephro.subscription.dto.ApplyPromoResponse;
import com.pedianephro.subscription.dto.PromoCodeRequest;
import com.pedianephro.subscription.dto.PromoCodeResponse;
import com.pedianephro.subscription.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/promo", "/promo"})
@CrossOrigin
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping("/create")
    public ResponseEntity<PromoCodeResponse> create(@Valid @RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.createPromoCode(request));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<PromoCodeResponse> validate(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.validatePromoCode(code));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplyPromoResponse> apply(@Valid @RequestBody ApplyPromoRequest request) {
        return ResponseEntity.ok(promoCodeService.applyPromoCode(request));
    }

    /** Liste des codes — même chose que {@link #all()} ; pratique derrière la gateway :8080. */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<PromoCodeResponse>> listRoot() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    @GetMapping("/all")
    public ResponseEntity<List<PromoCodeResponse>> all() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        promoCodeService.deactivatePromoCode(id);
        return ResponseEntity.ok().build();
    }
}

