package com.pedianephro.subscription.service;

import com.pedianephro.subscription.dto.ApplyPromoRequest;
import com.pedianephro.subscription.dto.ApplyPromoResponse;
import com.pedianephro.subscription.dto.PromoCodeRequest;
import com.pedianephro.subscription.dto.PromoCodeResponse;
import com.pedianephro.subscription.entity.PromoCode;
import com.pedianephro.subscription.repository.PromoCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private PromoCodeService promoCodeService;

    @Test
    void createPromoCode_shouldThrowConflict_whenCodeExists() {
        PromoCodeRequest req = new PromoCodeRequest("PEDIA2026", 30.0, 100, LocalDate.now().plusDays(10), true);
        when(promoCodeRepository.findByCode("PEDIA2026")).thenReturn(Optional.of(new PromoCode()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> promoCodeService.createPromoCode(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createPromoCode_shouldNormalizeCodeAndPersist() {
        PromoCodeRequest req = new PromoCodeRequest("  pedia2026  ", 30.0, 100, LocalDate.now().plusDays(10), null);
        when(promoCodeRepository.findByCode("PEDIA2026")).thenReturn(Optional.empty());
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> inv.getArgument(0));

        PromoCodeResponse res = promoCodeService.createPromoCode(req);

        assertNotNull(res);
        assertEquals("PEDIA2026", res.getCode());
        assertTrue(res.getValid());
        assertTrue(res.getActive());
        assertEquals(0, res.getCurrentUses());

        ArgumentCaptor<PromoCode> captor = ArgumentCaptor.forClass(PromoCode.class);
        verify(promoCodeRepository).save(captor.capture());
        PromoCode saved = captor.getValue();
        assertEquals("PEDIA2026", saved.getCode());
        assertEquals(30.0, saved.getDiscountPercent());
        assertEquals(100, saved.getMaxUses());
        assertEquals(0, saved.getCurrentUses());
        assertTrue(saved.isActive());
    }

    @Test
    void validatePromoCode_shouldReturnInvalid_whenNotFoundOrInactive() {
        when(promoCodeRepository.findByCodeAndActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        PromoCodeResponse res = promoCodeService.validatePromoCode("unknown");

        assertFalse(res.getValid());
        assertEquals("UNKNOWN", res.getCode());
        assertEquals("Code promo invalide", res.getMessage());
    }

    @Test
    void validatePromoCode_shouldReturnInvalid_whenExpired() {
        PromoCode p = new PromoCode();
        p.setId(1L);
        p.setCode("PEDIA2026");
        p.setDiscountPercent(30.0);
        p.setMaxUses(100);
        p.setCurrentUses(0);
        p.setExpiryDate(LocalDate.now().minusDays(1));
        p.setActive(true);

        when(promoCodeRepository.findByCodeAndActiveTrue("PEDIA2026")).thenReturn(Optional.of(p));

        PromoCodeResponse res = promoCodeService.validatePromoCode("PEDIA2026");

        assertFalse(res.getValid());
        assertTrue(res.getMessage().startsWith("Ce code promo a expiré le "));
    }

    @Test
    void validatePromoCode_shouldReturnInvalid_whenQuotaReached() {
        PromoCode p = new PromoCode();
        p.setId(1L);
        p.setCode("PEDIA2026");
        p.setDiscountPercent(30.0);
        p.setMaxUses(2);
        p.setCurrentUses(2);
        p.setExpiryDate(LocalDate.now().plusDays(10));
        p.setActive(true);

        when(promoCodeRepository.findByCodeAndActiveTrue("PEDIA2026")).thenReturn(Optional.of(p));

        PromoCodeResponse res = promoCodeService.validatePromoCode("PEDIA2026");

        assertFalse(res.getValid());
        assertEquals("Ce code promo a atteint sa limite d'utilisation", res.getMessage());
    }

    @Test
    void applyPromoCode_shouldReturnDiscountedPrice_whenValid() {
        PromoCode p = new PromoCode();
        p.setId(1L);
        p.setCode("PEDIA2026");
        p.setDiscountPercent(30.0);
        p.setMaxUses(100);
        p.setCurrentUses(0);
        p.setExpiryDate(LocalDate.now().plusDays(10));
        p.setActive(true);

        when(promoCodeRepository.findByCodeAndActiveTrue("PEDIA2026")).thenReturn(Optional.of(p));

        ApplyPromoResponse res = promoCodeService.applyPromoCode(new ApplyPromoRequest("PEDIA2026", 239.0));

        assertTrue(res.getValid());
        assertEquals(239.0, res.getOriginalPrice());
        assertEquals(167.3, res.getDiscountedPrice());
        assertEquals(71.7, res.getSavings());
        assertEquals("Code valide", res.getMessage());
    }

    @Test
    void applyPromoCode_shouldReturnOriginalPrice_whenInvalid() {
        when(promoCodeRepository.findByCodeAndActiveTrue("BAD")).thenReturn(Optional.empty());

        ApplyPromoResponse res = promoCodeService.applyPromoCode(new ApplyPromoRequest("bad", 100.0));

        assertFalse(res.getValid());
        assertEquals(100.0, res.getOriginalPrice());
        assertEquals(100.0, res.getDiscountedPrice());
        assertEquals(0.0, res.getSavings());
    }

    @Test
    void consumePromoCode_shouldIncrementUses_andDeactivateWhenMaxReached() {
        PromoCode p = new PromoCode();
        p.setId(1L);
        p.setCode("PEDIA2026");
        p.setDiscountPercent(30.0);
        p.setMaxUses(2);
        p.setCurrentUses(1);
        p.setExpiryDate(LocalDate.now().plusDays(10));
        p.setActive(true);

        when(promoCodeRepository.findActiveByCodeForUpdate("PEDIA2026")).thenReturn(Optional.of(p));
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> inv.getArgument(0));

        PromoCode saved = promoCodeService.consumePromoCode("PEDIA2026");

        assertEquals(2, saved.getCurrentUses());
        assertFalse(saved.isActive());
        verify(promoCodeRepository).save(eq(p));
    }

    @Test
    void consumePromoCode_shouldThrowBadRequest_whenNotFound() {
        when(promoCodeRepository.findActiveByCodeForUpdate("PEDIA2026")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> promoCodeService.consumePromoCode("PEDIA2026"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getAllPromoCodes_shouldReturnSortedList() {
        PromoCode a = new PromoCode();
        a.setId(1L);
        a.setCode("A");
        a.setDiscountPercent(10.0);
        a.setMaxUses(10);
        a.setCurrentUses(0);
        a.setExpiryDate(LocalDate.now().plusDays(10));
        a.setActive(true);

        PromoCode b = new PromoCode();
        b.setId(2L);
        b.setCode("B");
        b.setDiscountPercent(10.0);
        b.setMaxUses(10);
        b.setCurrentUses(0);
        b.setExpiryDate(LocalDate.now().plusDays(10));
        b.setActive(true);

        when(promoCodeRepository.findAll()).thenReturn(List.of(a, b));

        List<PromoCodeResponse> res = promoCodeService.getAllPromoCodes();

        assertEquals(2, res.size());
    }
}
