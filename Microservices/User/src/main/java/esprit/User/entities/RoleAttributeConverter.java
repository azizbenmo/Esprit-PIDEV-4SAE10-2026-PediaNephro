package esprit.User.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Évite les plantages Hibernate / face-login quand la colonne {@code users.role}
 * contient une valeur vide, une casse incorrecte ou une ancienne valeur hors enum
 * (ex. {@code No enum constant esprit.User.entities.Role.}).
 */
@Converter(autoApply = false)
public class RoleAttributeConverter implements AttributeConverter<Role, String> {

    private static final Logger log = LoggerFactory.getLogger(RoleAttributeConverter.class);

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute == null ? Role.PARENT.name() : attribute.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            log.warn("users.role vide ou null en base — affectation PARENT par defaut");
            return Role.PARENT;
        }
        String normalized = dbData.trim().toUpperCase();
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            log.warn("users.role valeur inconnue '{}' — affectation PARENT par defaut", dbData);
            return Role.PARENT;
        }
    }
}
