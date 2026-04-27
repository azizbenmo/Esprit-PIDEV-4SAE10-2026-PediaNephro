package com.pedianephro.subscription.integration;

import com.pedianephro.subscription.client.UserServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Vérifie auprès du MS User qu'un identifiant utilisateur existe (OpenFeign + Eureka).
 */
@Component
@RequiredArgsConstructor
public class UserMsValidator {

    private final UserServiceClient userServiceClient;

    @Value("${subscription.integration.user-validation-enabled:true}")
    private boolean validationEnabled;

    public void ensureUserExists(Long userId) {
        if (!validationEnabled || userId == null) {
            return;
        }
        try {
            userServiceClient.getUserById(userId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable (microservice utilisateur).");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Microservice utilisateur indisponible ou erreur réseau: " + e.getMessage()
            );
        }
    }
}
