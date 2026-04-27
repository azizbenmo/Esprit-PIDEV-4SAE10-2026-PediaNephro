package tn.example.events.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.example.events.dto.UserServiceSummaryDto;

import java.util.Optional;

@Service
public class UserMicroserviceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.user-service.base-url:http://127.0.0.1:8083}")
    private String userServiceBaseUrl;

    public Optional<UserServiceSummaryDto> getUserById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String base = userServiceBaseUrl.endsWith("/")
                ? userServiceBaseUrl.substring(0, userServiceBaseUrl.length() - 1)
                : userServiceBaseUrl;
        String url = base + "/mic1/internal/users/" + id;
        try {
            ResponseEntity<UserServiceSummaryDto> response =
                    restTemplate.getForEntity(url, UserServiceSummaryDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new RuntimeException("Service utilisateur indisponible : " + e.getMessage(), e);
        }
    }
}
