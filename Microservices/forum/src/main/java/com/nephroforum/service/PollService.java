package com.nephroforum.service;

import com.nephroforum.dto.PollDTOs;
import com.nephroforum.entity.*;
import com.nephroforum.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepo;
    private final PollVoteRepository voteRepo;

    @Transactional
    public PollDTOs.PollResponse createPoll(PollDTOs.CreatePollRequest req) {
        Poll poll = Poll.builder()
                .question(req.question())
                .createdBy(req.createdBy())
                .expiresAt(req.expiresAt())
                .build();

        for (String text : req.options()) {
            PollOption opt = PollOption.builder()
                    .text(text)
                    .poll(poll)
                    .build();
            poll.getOptions().add(opt);
        }

        return toResponse(pollRepo.save(poll), null);
    }

    public List<PollDTOs.PollResponse> getActivePolls(String voterName) {
        return pollRepo.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(p -> toResponse(p, voterName))
                .collect(Collectors.toList());
    }

    @Transactional
    public PollDTOs.PollResponse vote(Long pollId, Long optionId, String voterName) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        // Si le patient a déjà voté pour cette option → ne rien faire
        boolean alreadyVotedSameOption = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .map(o -> o.getVotes().stream()
                        .anyMatch(v -> v.getVoterName().equals(voterName)))
                .orElse(false);

        if (alreadyVotedSameOption) {
            return toResponse(poll, voterName);
        }

        // Supprimer l'ancien vote du patient s'il existe sur une autre option
        if (voteRepo.existsByVoterNameAndOptionPollId(voterName, pollId)) {
            voteRepo.deleteByVoterNameAndOptionPollId(voterName, pollId);
            voteRepo.flush();
            // Recharger le poll après suppression
            poll = pollRepo.findById(pollId)
                    .orElseThrow(() -> new RuntimeException("Poll not found"));
        }

        // Ajouter le nouveau vote
        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Option not found"));

        PollVote vote = PollVote.builder()
                .voterName(voterName)
                .option(option)
                .build();
        option.getVotes().add(vote);

        return toResponse(pollRepo.save(poll), voterName);
    }

    private PollDTOs.PollResponse toResponse(Poll poll, String voterName) {
        int total = poll.getOptions().stream()
                .mapToInt(o -> o.getVotes().size()).sum();

        String userVotedOptionId = null;
        if (voterName != null) {
            for (PollOption opt : poll.getOptions()) {
                boolean voted = opt.getVotes().stream()
                        .anyMatch(v -> v.getVoterName().equals(voterName));
                if (voted) { userVotedOptionId = opt.getId().toString(); break; }
            }
        }

        List<PollDTOs.OptionResponse> options = poll.getOptions().stream()
                .map(o -> PollDTOs.OptionResponse.builder()
                        .id(o.getId())
                        .text(o.getText())
                        .votes(o.getVotes().size())
                        .percentage(total > 0 ? (o.getVotes().size() * 100.0 / total) : 0)
                        .build())
                .collect(Collectors.toList());

        return PollDTOs.PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .createdBy(poll.getCreatedBy())
                .active(poll.isActive())
                .options(options)
                .totalVotes(total)
                .createdAt(poll.getCreatedAt())
                .expiresAt(poll.getExpiresAt())
                .userVotedOptionId(userVotedOptionId)
                .build();
    }

    @Transactional
    public PollDTOs.PollResponse updatePoll(Long pollId, PollDTOs.UpdatePollRequest req) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        if (req.question() != null) poll.setQuestion(req.question());
        if (req.expiresAt() != null) poll.setExpiresAt(req.expiresAt());

        // Modifier les options
        if (req.options() != null && !req.options().isEmpty()) {
            poll.getOptions().clear();
            for (String text : req.options()) {
                PollOption opt = PollOption.builder()
                        .text(text)
                        .poll(poll)
                        .build();
                poll.getOptions().add(opt);
            }
        }
        return toResponse(pollRepo.save(poll), null);
    }

    @Transactional
    public void closePoll(Long pollId) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        poll.setActive(false);
        poll.setArchived(true);
        pollRepo.save(poll);
    }

    public List<PollDTOs.PollResponse> getArchivedPolls() {
        return pollRepo.findByArchivedTrueOrderByCreatedAtDesc()
                .stream()
                .map(p -> toResponse(p, null))
                .collect(Collectors.toList());
    }
}