package com.nephroforum.dto;

import com.nephroforum.entity.Report.ReportReason;
import com.nephroforum.entity.Report.ReportStatus;
import lombok.*;
import java.time.LocalDateTime;

public class ReportDTOs {

    public record CreateReportRequest(
            String reporterName,
            Long postId,
            Long commentId,
            ReportReason reason,
            String details
    ) {}

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReportResponse {
        private Long id;
        private String reporterName;
        private Long postId;
        private Long commentId;
        private ReportReason reason;
        private String details;
        private ReportStatus status;
        private LocalDateTime createdAt;
        private Long linkedPostId;
    }
}