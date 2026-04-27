package com.nephroforum.service;

import com.nephroforum.entity.BannedWord;
import com.nephroforum.entity.BannedWord.WordType;
import com.nephroforum.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final BannedWordRepository bannedWordRepo;

    // Mots par défaut insérés au démarrage si la table est vide
    @jakarta.annotation.PostConstruct
    public void initDefaults() {
        if (bannedWordRepo.count() == 0) {
            List<String> defaultBadWords = List.of(
                    "idiot", "stupid", "moron", "hate", "dumb", "ugly",
                    "loser", "jerk", "shut up", "kill"
            );
            List<String> defaultMedicines = List.of(
                    "ibuprofen", "aspirin", "metformin", "lisinopril",
                    "prednisone", "prednisolone", "cyclosporine", "tacrolimus",
                    "mycophenolate", "furosemide", "enalapril", "amlodipine",
                    "ramipril", "azathioprine", "rituximab", "hydroxychloroquine",
                    "methylprednisolone", "dexamethasone", "hydrocortisone",
                    "levothyroxine", "omeprazole", "pantoprazole"
            );

            defaultBadWords.forEach(w -> bannedWordRepo.save(
                    BannedWord.builder().word(w).type(WordType.BAD_WORD).build()
            ));
            defaultMedicines.forEach(m -> bannedWordRepo.save(
                    BannedWord.builder().word(m).type(WordType.MEDICINE).build()
            ));
        }
    }

    public boolean isAllowed(String text) {
        if (text == null || text.isBlank()) return true;
        String lower = text.toLowerCase();

        List<String> allWords = bannedWordRepo.findAll()
                .stream()
                .map(BannedWord::getWord)
                .collect(Collectors.toList());

        return allWords.stream().noneMatch(lower::contains);
    }

    public String getViolationReason(String text) {
        if (text == null || text.isBlank()) return null;
        String lower = text.toLowerCase();

        for (BannedWord bw : bannedWordRepo.findAll()) {
            if (lower.contains(bw.getWord())) {
                return bw.getType() + ": " + bw.getWord();
            }
        }
        return null;
    }

    // ── Bad Words ─────────────────────────────────────────────────────────────
    public List<String> getBadWords() {
        return bannedWordRepo.findByType(WordType.BAD_WORD)
                .stream().map(BannedWord::getWord).collect(Collectors.toList());
    }

    @Transactional
    public void addBadWord(String word) {
        String w = word.toLowerCase().trim();
        if (!bannedWordRepo.existsByWordAndType(w, WordType.BAD_WORD)) {
            bannedWordRepo.save(BannedWord.builder()
                    .word(w).type(WordType.BAD_WORD).build());
        }
    }

    @Transactional
    public void removeBadWord(String word) {
        bannedWordRepo.deleteByWordAndType(word.toLowerCase().trim(), WordType.BAD_WORD);
    }

    // ── Medicines ─────────────────────────────────────────────────────────────
    public List<String> getMedicines() {
        return bannedWordRepo.findByType(WordType.MEDICINE)
                .stream().map(BannedWord::getWord).collect(Collectors.toList());
    }

    @Transactional
    public void addMedicine(String medicine) {
        String m = medicine.toLowerCase().trim();
        if (!bannedWordRepo.existsByWordAndType(m, WordType.MEDICINE)) {
            bannedWordRepo.save(BannedWord.builder()
                    .word(m).type(WordType.MEDICINE).build());
        }
    }

    @Transactional
    public void removeMedicine(String medicine) {
        bannedWordRepo.deleteByWordAndType(medicine.toLowerCase().trim(), WordType.MEDICINE);
    }
}