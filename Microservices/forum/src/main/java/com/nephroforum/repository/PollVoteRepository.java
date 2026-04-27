package com.nephroforum.repository;

import com.nephroforum.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByVoterNameAndOptionPollId(String voterName, Long pollId);
    void deleteByVoterNameAndOptionPollId(String voterName, Long pollId);
}