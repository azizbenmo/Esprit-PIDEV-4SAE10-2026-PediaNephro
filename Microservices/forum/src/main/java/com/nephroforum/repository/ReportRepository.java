package com.nephroforum.repository;

import com.nephroforum.entity.Report;
import com.nephroforum.entity.Report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    boolean existsByReporterNameAndPostId(String reporterName, Long postId);

    boolean existsByReporterNameAndCommentId(String reporterName, Long commentId);

    long countByStatus(Report.ReportStatus status);
}