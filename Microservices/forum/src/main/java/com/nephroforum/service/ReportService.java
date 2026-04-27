package com.nephroforum.service;

import com.nephroforum.dto.ReportDTOs;
import com.nephroforum.entity.Comment;
import com.nephroforum.entity.Post;
import com.nephroforum.entity.Report;
import com.nephroforum.entity.Report.ReportStatus;
import com.nephroforum.exception.BadRequestException;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.CommentRepository;
import com.nephroforum.repository.PostRepository;
import com.nephroforum.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepo;
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final BanService banService;

    @Transactional
    public ReportDTOs.ReportResponse createReport(ReportDTOs.CreateReportRequest req) {
        if (req.postId() == null && req.commentId() == null)
            throw new BadRequestException("postId ou commentId est requis.");

        if (req.postId() != null &&
                reportRepo.existsByReporterNameAndPostId(req.reporterName(), req.postId()))
            throw new BadRequestException("Vous avez déjà signalé ce post.");

        if (req.commentId() != null &&
                reportRepo.existsByReporterNameAndCommentId(req.reporterName(), req.commentId()))
            throw new BadRequestException("Vous avez déjà signalé ce commentaire.");

        Report report = Report.builder()
                .reporterName(req.reporterName())
                .postId(req.postId())
                .commentId(req.commentId())
                .reason(req.reason())
                .details(req.details())
                .build();

        return toResponse(reportRepo.save(report));
    }

    public List<ReportDTOs.ReportResponse> getPending() {
        return reportRepo.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReportDTOs.ReportResponse updateStatus(Long reportId, ReportStatus status) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report not found: " + reportId));
        report.setStatus(status);
        return toResponse(reportRepo.save(report));
    }

    @Transactional
    public void resolveAndDelete(Long reportId) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report not found: " + reportId));

        String violatingUser = null;

        // Supprime le post
        if (report.getPostId() != null) {
            Optional<Post> post = postRepo.findById(report.getPostId());
            if (post.isPresent()) {
                violatingUser = post.get().getAuthorName();
                post.get().setDeleted(true);
                postRepo.save(post.get());
            }
        }

        // Supprime le commentaire
        if (report.getCommentId() != null) {
            Optional<Comment> comment = commentRepo.findById(report.getCommentId());
            if (comment.isPresent()) {
                violatingUser = comment.get().getAuthorName();
                commentRepo.deleteById(report.getCommentId());
            }
        }

        // Ajoute une violation à l'auteur du contenu supprimé
        if (violatingUser != null && !violatingUser.equals("Patient Anonyme")) {
            banService.addViolation(violatingUser);
        }

        report.setStatus(ReportStatus.RESOLVED);
        reportRepo.save(report);
    }

    private ReportDTOs.ReportResponse toResponse(Report report) {
        Long linkedPostId = report.getPostId();

        if (report.getCommentId() != null && linkedPostId == null) {
            linkedPostId = commentRepo.findById(report.getCommentId())
                    .map(c -> c.getPost().getId())
                    .orElse(null);
        }

        return ReportDTOs.ReportResponse.builder()
                .id(report.getId())
                .reporterName(report.getReporterName())
                .postId(report.getPostId())
                .commentId(report.getCommentId())
                .linkedPostId(linkedPostId)
                .reason(report.getReason())
                .details(report.getDetails())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}