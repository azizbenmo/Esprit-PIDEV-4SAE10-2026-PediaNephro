package com.nephroforum.service;

import com.nephroforum.dto.PostDTOs;
import com.nephroforum.entity.*;
import com.nephroforum.exception.ContentModeratedException;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.nephroforum.dto.SearchDTOs;
import com.nephroforum.entity.Report;
import com.nephroforum.repository.ReportRepository;
import com.nephroforum.repository.PollRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository       postRepo;
    private final TagRepository        tagRepo;
    private final ReactionRepository   reactionRepo;
    private final ModerationService    moderationService;
    private final BadgeService         badgeService;
    private final CommentRepository    commentRepo;
    private final ReportRepository     reportRepo;
    private final PollRepository       pollRepo;
    private final PostViewRepository   postViewRepo;
    private final AIService            aiService;
    private final FollowService        followService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── Créer un post ─────────────────────────────────────────────────────────

    @Transactional
    public PostDTOs.PostResponse createPost(String title, String description,
                                            List<String> tagNames, String authorName,
                                            boolean anonymous,
                                            MultipartFile image) throws IOException {

        if (!moderationService.isAllowed(title) || !moderationService.isAllowed(description))
            throw new ContentModeratedException("Post contains prohibited content.");

        String imageUrl = null;
        if (image != null && !image.isEmpty())
            imageUrl = saveImage(image);

        Post post = Post.builder()
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .authorName(authorName)
                .anonymous(anonymous)
                .build();

        if (tagNames != null) {
            Set<Tag> tags = new HashSet<>();
            for (String name : tagNames) {
                String trimmed = name.trim().toLowerCase();
                Tag tag = tagRepo.findByName(trimmed)
                        .orElseGet(() -> tagRepo.save(
                                Tag.builder().name(trimmed).build()));
                tags.add(tag);
            }
            post.setTags(tags);
        }

        PostDTOs.PostResponse response = toResponse(postRepo.save(post));

        // Badges
        badgeService.checkAndAwardBadges(authorName);

        // Notifier les followers (sauf si post anonyme)
        if (!anonymous) {
            followService.notifyFollowers(authorName, response.getId(), title);
        }

        return response;
    }

    // ── Upload image d'un post ────────────────────────────────────────────────

    @Transactional
    public PostDTOs.PostResponse uploadImage(Long id, MultipartFile image) throws IOException {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setImageUrl(saveImage(image));
        return toResponse(postRepo.save(post));
    }

    // ── Liste des posts (épinglés en premier) ─────────────────────────────────

    public Page<PostDTOs.PostResponse> getPosts(String keyword, String sort,
                                                int page, int size) {
        String kw = (keyword == null) ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, size);

        return switch (sort.toUpperCase()) {
            case "OLDEST" -> postRepo.searchPostsOldest(kw, pageable)
                    .map(this::toResponse);
            case "HOT"    -> postRepo.findHotPosts(kw, pageable)
                    .map(this::toResponse);
            default       -> postRepo.searchPostsRecent(kw, pageable)
                    .map(this::toResponse);
        };
    }

    // ── Détail d'un post (comptage de vues unique) ────────────────────────────

    @Transactional
    public PostDTOs.PostResponse getPost(Long id, String viewerName) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        if (viewerName != null && !viewerName.isBlank() &&
                !postViewRepo.existsByPostIdAndViewerName(id, viewerName)) {
            post.setViews(post.getViews() + 1);
            postRepo.save(post);
            postViewRepo.save(PostView.builder()
                    .postId(id)
                    .viewerName(viewerName)
                    .build());
        }

        return toResponse(post);
    }

    // ── Modifier un post ──────────────────────────────────────────────────────

    @Transactional
    public PostDTOs.PostResponse updatePost(Long id, String title,
                                            String description,
                                            List<String> tagNames) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));

        if (title != null) {
            if (!moderationService.isAllowed(title))
                throw new ContentModeratedException("Title contains prohibited content.");
            post.setTitle(title);
        }
        if (description != null) {
            if (!moderationService.isAllowed(description))
                throw new ContentModeratedException("Description contains prohibited content.");
            post.setDescription(description);
        }
        if (tagNames != null) {
            Set<Tag> tags = new HashSet<>();
            for (String name : tagNames) {
                String trimmed = name.trim().toLowerCase();
                Tag tag = tagRepo.findByName(trimmed)
                        .orElseGet(() -> tagRepo.save(
                                Tag.builder().name(trimmed).build()));
                tags.add(tag);
            }
            post.setTags(tags);
        }
        return toResponse(postRepo.save(post));
    }

    // ── Supprimer un post (soft delete) ───────────────────────────────────────

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setDeleted(true);
        postRepo.save(post);
    }

    // ── Épingler / Désépingler ────────────────────────────────────────────────

    @Transactional
    public PostDTOs.PostResponse pinPost(Long id, String doctorName, boolean pin) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setPinned(pin);
        post.setPinnedBy(pin ? doctorName : null);
        post.setPinnedAt(pin ? LocalDateTime.now() : null);
        return toResponse(postRepo.save(post));
    }

    // ── Posts par auteur ──────────────────────────────────────────────────────

    public List<PostDTOs.PostResponse> getPostsByAuthor(String authorName,
                                                        Long excludeId) {
        return postRepo.findByAuthorNameAndIdNot(authorName, excludeId)
                .stream()
                .limit(5)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Recherche avancée ─────────────────────────────────────────────────────

    public Page<PostDTOs.PostResponse> advancedSearch(SearchDTOs.SearchRequest req) {
        LocalDateTime dateFrom = req.getDateFrom() != null
                ? req.getDateFrom().atStartOfDay() : null;
        LocalDateTime dateTo = req.getDateTo() != null
                ? req.getDateTo().atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of(
                req.getPage(),
                req.getSize() > 0 ? req.getSize() : 10
        );

        Page<Post> posts = postRepo.advancedSearch(
                req.getKeyword(),
                req.getTag(),
                dateFrom,
                dateTo,
                req.getHasImage(),
                req.getAnonymous(),
                pageable
        );

        if (req.getHasResponse() != null) {
            List<Post> filtered = posts.getContent().stream()
                    .filter(p -> req.getHasResponse()
                            ? !p.getComments().isEmpty()
                            : p.getComments().isEmpty())
                    .collect(Collectors.toList());

            return new PageImpl<>(
                    filtered.stream().map(this::toResponse).collect(Collectors.toList()),
                    pageable,
                    filtered.size()
            );
        }

        return posts.map(this::toResponse);
    }

    // ── Statistiques ──────────────────────────────────────────────────────────

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts",        postRepo.count());
        stats.put("activePosts",       postRepo.countByDeletedFalse());
        stats.put("deletedPosts",      postRepo.count() - postRepo.countByDeletedFalse());
        stats.put("anonymousPosts",    postRepo.countByAnonymousTrue());
        stats.put("totalComments",     commentRepo.count());
        stats.put("moderatedComments", commentRepo.countByModeratedTrue());
        stats.put("pendingReports",    reportRepo.countByStatus(Report.ReportStatus.PENDING));
        stats.put("resolvedReports",   reportRepo.countByStatus(Report.ReportStatus.RESOLVED));
        stats.put("dismissedReports",  reportRepo.countByStatus(Report.ReportStatus.DISMISSED));
        stats.put("activePolls",       pollRepo.countByActiveTrue());
        stats.put("archivedPolls",     pollRepo.countByArchivedTrue());
        return stats;
    }

    // ── Résumé IA ─────────────────────────────────────────────────────────────

    public Map<String, String> summarizePost(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        String summary = aiService.summarizePost(
                post.getTitle(),
                post.getDescription(),
                post.getComments().size()
        );
        return Map.of("summary", summary);
    }

    // ── Sauvegarde image ──────────────────────────────────────────────────────

    private String saveImage(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }

    // ── toResponse ────────────────────────────────────────────────────────────

    public PostDTOs.PostResponse toResponse(Post post) {
        Map<String, Long> reactionCounts = new HashMap<>();
        for (Reaction.ReactionType r : Reaction.ReactionType.values())
            reactionCounts.put(r.name(), 0L);
        for (Object[] entry : reactionRepo.countByPostId(post.getId()))
            reactionCounts.put(entry[0].toString(), (Long) entry[1]);

        String displayName = post.isAnonymous()
                ? "Patient Anonyme" : post.getAuthorName();

        boolean hasDoctorResponse = post.getComments().stream()
                .anyMatch(c -> !c.isModerated() &&
                        (c.getAuthorName().startsWith("Dr.") ||
                                c.getAuthorName().startsWith("dr.")));

        return PostDTOs.PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .imageUrl(post.getImageUrl())
                .authorName(post.getAuthorName())
                .displayName(displayName)
                .anonymous(post.isAnonymous())
                .tags(post.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .reactions(reactionCounts)
                .commentCount(post.getComments().stream()
                        .filter(c -> !c.isModerated()).count())
                .hasDoctorResponse(hasDoctorResponse)
                .views(post.getViews())
                .pinned(post.isPinned())
                .pinnedBy(post.getPinnedBy())
                .pinnedAt(post.getPinnedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}