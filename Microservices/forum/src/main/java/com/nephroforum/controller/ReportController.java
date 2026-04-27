package com.nephroforum.controller;

import com.nephroforum.dto.ReportDTOs;
import com.nephroforum.entity.Report.ReportStatus;
import com.nephroforum.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Signaler un post ou commentaire
    @PostMapping
    public ResponseEntity<ReportDTOs.ReportResponse> create(
            @RequestBody ReportDTOs.CreateReportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(req));
    }

    // Voir tous les signalements en attente (backoffice)
    @GetMapping("/pending")
    public ResponseEntity<List<ReportDTOs.ReportResponse>> getPending() {
        return ResponseEntity.ok(reportService.getPending());
    }

    // Mettre à jour le statut (backoffice)
    @PutMapping("/{reportId}/status")
    public ResponseEntity<ReportDTOs.ReportResponse> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status) {
        return ResponseEntity.ok(reportService.updateStatus(reportId, status));
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Long reportId) {
        reportService.resolveAndDelete(reportId);
        return ResponseEntity.ok().build();
    }
}
