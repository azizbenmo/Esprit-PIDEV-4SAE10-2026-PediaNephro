package com.nephroforum;

import com.nephroforum.entity.QuickReply;
import com.nephroforum.repository.QuickReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuickReplySeeder implements CommandLineRunner {

    private final QuickReplyRepository quickReplyRepo;

    @Override
    public void run(String... args) {
        if (quickReplyRepo.count() == 0) {
            quickReplyRepo.save(QuickReply.builder()
                    .label("Consultez un spécialiste")
                    .content("Je vous recommande de consulter un spécialiste " +
                            "dès que possible pour une évaluation approfondie. " +
                            "N'hésitez pas à prendre rendez-vous rapidement.")
                    .ownerName("Dr.Amina").build());

            quickReplyRepo.save(QuickReply.builder()
                    .label("Ceci est normal")
                    .content("Ce que vous décrivez est tout à fait normal à ce stade " +
                            "de la maladie. Continuez le traitement prescrit et " +
                            "signalez tout changement lors de votre prochaine consultation.")
                    .ownerName("Dr.Amina").build());

            quickReplyRepo.save(QuickReply.builder()
                    .label("Prenez rendez-vous")
                    .content("Je vous conseille de prendre rendez-vous rapidement " +
                            "pour que nous puissions examiner votre enfant et " +
                            "ajuster le traitement si nécessaire.")
                    .ownerName("Dr.Amina").build());

            quickReplyRepo.save(QuickReply.builder()
                    .label("Surveillez les symptômes")
                    .content("Surveillez attentivement l'évolution des symptômes. " +
                            "Si vous observez une aggravation ou de nouveaux symptômes, " +
                            "contactez-nous immédiatement sans attendre le prochain rendez-vous.")
                    .ownerName("Dr.Amina").build());

            quickReplyRepo.save(QuickReply.builder()
                    .label("Rassurez-vous")
                    .content("Je comprends votre inquiétude, mais rassurez-vous. " +
                            "Ce type de situation est fréquent et gérable. " +
                            "Nous allons vous accompagner pas à pas dans cette étape.")
                    .ownerName("Dr.Amina").build());

            System.out.println("✅ Réponses rapides initialisées pour Dr.Amina");
        }
    }
}