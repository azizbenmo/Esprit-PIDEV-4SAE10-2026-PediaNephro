package com.nephroforum.service;

import com.nephroforum.dto.QuickReplyDTOs;
import com.nephroforum.entity.QuickReply;
import com.nephroforum.exception.ResourceNotFoundException;
import com.nephroforum.repository.QuickReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuickReplyService {

    private final QuickReplyRepository quickReplyRepo;

    public List<QuickReplyDTOs.QuickReplyResponse> getByOwner(String ownerName) {
        return quickReplyRepo.findByOwnerName(ownerName)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public QuickReplyDTOs.QuickReplyResponse create(
            QuickReplyDTOs.CreateQuickReplyRequest req) {
        QuickReply qr = QuickReply.builder()
                .label(req.label())
                .content(req.content())
                .ownerName(req.ownerName())
                .build();
        return toResponse(quickReplyRepo.save(qr));
    }

    @Transactional
    public void delete(Long id) {
        quickReplyRepo.deleteById(id);
    }

    private QuickReplyDTOs.QuickReplyResponse toResponse(QuickReply qr) {
        return QuickReplyDTOs.QuickReplyResponse.builder()
                .id(qr.getId())
                .label(qr.getLabel())
                .content(qr.getContent())
                .ownerName(qr.getOwnerName())
                .build();
    }
}