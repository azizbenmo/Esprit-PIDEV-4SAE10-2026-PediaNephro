package esprit.User.services;

import esprit.User.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service qui interagit avec l'Admin REST API de Keycloak.
 *
 * Fonctionnalités :
 *  - Obtenir un token admin (client_credentials)
 *  - Créer un utilisateur dans Keycloak
 *  - Assigner un rôle realm selon l'enum Role de l'application
 */
@Service
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────────────────────────────────────────────────────────
    // 1. Obtenir un token admin via client_credentials
    // ─────────────────────────────────────────────────────────────────

    /**
     * Retourne un access_token admin pour appeler l'Admin REST API de Keycloak.
     * Utilise le flux OAuth2 "client_credentials".
     */
    public String getAdminToken() {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("[Keycloak] Impossible d'obtenir le token admin : {}", e.getMessage());
            throw new RuntimeException("Keycloak admin token non disponible", e);
        }

        throw new RuntimeException("Keycloak token vide dans la réponse");
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. Créer un utilisateur dans Keycloak
    // ─────────────────────────────────────────────────────────────────

    /**
     * Crée un utilisateur dans Keycloak.
     * Si l'utilisateur existe déjà (409), on ignore l'erreur et on retourne son ID.
     *
     * @param username  nom d'utilisateur
     * @param email     email
     * @param password  mot de passe en clair (Keycloak le hash)
     * @param role      rôle applicatif (ADMIN, DOCTOR, PARENT, PATIENT)
     */
    public void createUserInKeycloak(String username, String email, String password, Role role) {
        String adminToken = getAdminToken();
        String usersUrl = serverUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        // Corps de la requête de création utilisateur
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );

        Map<String, Object> userRepresentation = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRepresentation, headers);

        String keycloakUserId = null;

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(usersUrl, request, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Récupérer l'ID depuis l'en-tête Location: /admin/realms/{realm}/users/{uuid}
                String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
                if (location != null) {
                    keycloakUserId = location.substring(location.lastIndexOf('/') + 1);
                    log.info("[Keycloak] Utilisateur '{}' créé avec id={}", username, keycloakUserId);
                }
            }
        } catch (HttpClientErrorException.Conflict ex) {
            // L'utilisateur existe déjà → récupérer son ID
            log.warn("[Keycloak] L'utilisateur '{}' existe déjà, récupération de son ID...", username);
            keycloakUserId = findKeycloakUserId(adminToken, username);
        } catch (Exception e) {
            log.error("[Keycloak] Erreur à la création de l'utilisateur '{}' : {}", username, e.getMessage());
            // Ne pas bloquer l'inscription si Keycloak est indisponible
            return;
        }

        // Assigner le rôle si on a l'ID
        if (keycloakUserId != null) {
            assignRealmRole(adminToken, keycloakUserId, role);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. Trouver l'UUID Keycloak d'un utilisateur par username
    // ─────────────────────────────────────────────────────────────────

    private String findKeycloakUserId(String adminToken, String username) {
        String searchUrl = serverUrl + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(searchUrl, HttpMethod.GET, request, List.class);
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && !response.getBody().isEmpty()) {
                Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
                return (String) user.get("id");
            }
        } catch (Exception e) {
            log.error("[Keycloak] Recherche utilisateur '{}' échouée : {}", username, e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. Assigner un rôle realm selon l'enum Role applicatif
    // ─────────────────────────────────────────────────────────────────

    /**
     * Assigne le rôle Keycloak correspondant à l'enum Role de l'application.
     * Les rôles doivent exister dans le realm Keycloak avec les mêmes noms.
     */
    public void assignRealmRole(String adminToken, String keycloakUserId, Role role) {
        // Mapper enum applicatif → nom de rôle Keycloak
        String roleName = mapRoleToKeycloak(role);
        if (roleName == null) {
            log.warn("[Keycloak] Aucun rôle Keycloak mappé pour le rôle applicatif '{}'", role);
            return;
        }

        // 4a. Obtenir l'objet RoleRepresentation depuis Keycloak
        String roleUrl = serverUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> getRequest = new HttpEntity<>(headers);

        Map<String, Object> roleRepresentation;
        try {
            ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, getRequest, Map.class);
            roleRepresentation = roleResponse.getBody();
        } catch (Exception e) {
            log.error("[Keycloak] Rôle '{}' introuvable dans le realm '{}' : {}", roleName, realm, e.getMessage());
            return;
        }

        // 4b. Assigner le rôle à l'utilisateur
        String assignUrl = serverUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/role-mappings/realm";
        HttpHeaders assignHeaders = new HttpHeaders();
        assignHeaders.setContentType(MediaType.APPLICATION_JSON);
        assignHeaders.setBearerAuth(adminToken);

        HttpEntity<List<Map<String, Object>>> assignRequest =
                new HttpEntity<>(List.of(roleRepresentation), assignHeaders);

        try {
            restTemplate.exchange(assignUrl, HttpMethod.POST, assignRequest, Void.class);
            log.info("[Keycloak] Rôle '{}' assigné à l'utilisateur id='{}'", roleName, keycloakUserId);
        } catch (Exception e) {
            log.error("[Keycloak] Erreur assignation rôle '{}' : {}", roleName, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. Mapping enum Role → nom de rôle Keycloak
    // ─────────────────────────────────────────────────────────────────

    private String mapRoleToKeycloak(Role role) {
        return switch (role) {
            case ADMIN   -> "ADMIN";
            case DOCTOR  -> "DOCTOR";
            case PARENT  -> "PARENT";
            case PATIENT -> "PARENT"; // PATIENT géré via PARENT dans Keycloak
        };
    }
}
