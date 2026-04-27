package com.pedianephro.subscription.controller;

import com.pedianephro.subscription.dto.AdjustmentProposalResponse;
import com.pedianephro.subscription.dto.AdjustmentType;
import com.pedianephro.subscription.service.AdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/adjustment", "/adjustment"})
@CrossOrigin
@RequiredArgsConstructor
public class AdjustmentController {

    private final AdjustmentService adjustmentService;

    @GetMapping("/check/{userId}")
    public ResponseEntity<AdjustmentProposalResponse> check(@PathVariable Long userId) {
        return ResponseEntity.ok(adjustmentService.check(userId));
    }

    @GetMapping("/check-all")
    public ResponseEntity<List<AdjustmentProposalResponse>> checkAll() {
        return ResponseEntity.ok(adjustmentService.checkAll());
    }

    @GetMapping("/simulate/{userId}")
    public ResponseEntity<AdjustmentProposalResponse> simulate(@PathVariable Long userId,
                                                               @RequestParam("scenario") AdjustmentType scenario) {
        return ResponseEntity.ok(adjustmentService.simulate(userId, scenario));
    }
}

