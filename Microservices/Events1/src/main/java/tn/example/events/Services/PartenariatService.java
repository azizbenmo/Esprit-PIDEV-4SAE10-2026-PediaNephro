    package tn.example.events.Services;

    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import tn.example.events.Entities.Partenariat;
    import tn.example.events.Entities.StatutPartenariat;
    import tn.example.events.Repositories.PartenariatRepository;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PartenariatService {

        private final PartenariatRepository partenariatRepository;
        private final MailService mailService; // ✅ ajouté


        public List<Partenariat> getAll() {
            return partenariatRepository.findAll();
        }

        public List<Partenariat> getAcceptes() {
            return partenariatRepository.findByStatut(StatutPartenariat.ACCEPTE);
        }

        public Partenariat getById(Long id) {
            return partenariatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Partenariat not found"));
        }

        /*public Partenariat create(Partenariat partenariat) {
            partenariat.setStatut(StatutPartenariat.EN_ATTENTE);
            return partenariatRepository.save(partenariat);
        }*/
        public Partenariat create(Partenariat partenariat) {
            partenariat.setStatut(StatutPartenariat.EN_ATTENTE);
            Partenariat saved = partenariatRepository.save(partenariat);

            // Envoie les deux mails
            mailService.sendDemandePartenariatToAdmin(saved);
            mailService.sendConfirmationToEntreprise(saved);

            return saved;
        }

        /*public Partenariat updateStatut(Long id, StatutPartenariat statut) {
            Partenariat p = getById(id);
            p.setStatut(statut);
            return partenariatRepository.save(p);
        }*/
        public Partenariat updateStatut(Long id, StatutPartenariat statut) {
            Partenariat p = getById(id);
            p.setStatut(statut);
            Partenariat saved = partenariatRepository.save(p);

            // Notifie l'entreprise du changement de statut
            mailService.sendStatutUpdateToEntreprise(saved);

            return saved;
        }

        public void delete(Long id) {
            partenariatRepository.deleteById(id);
        }

        public Partenariat findById(Long id) {
            return partenariatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Partenariat introuvable : " + id));
        }
    }