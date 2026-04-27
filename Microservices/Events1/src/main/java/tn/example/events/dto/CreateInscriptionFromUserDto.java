package tn.example.events.dto;

import tn.example.events.Entities.TypeParticipant;

/**
 * Inscription front : référence un compte User (médecin ou patient) au lieu d’un id Participant existant.
 */
public class CreateInscriptionFromUserDto {

    private Long eventId;
    private Long userId;
    private TypeParticipant typeParticipant;
    private String fullName;
    private String email;
    private String telephone;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TypeParticipant getTypeParticipant() {
        return typeParticipant;
    }

    public void setTypeParticipant(TypeParticipant typeParticipant) {
        this.typeParticipant = typeParticipant;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
