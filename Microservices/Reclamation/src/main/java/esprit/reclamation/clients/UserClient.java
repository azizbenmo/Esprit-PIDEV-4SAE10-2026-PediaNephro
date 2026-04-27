package esprit.reclamation.clients;

import esprit.reclamation.dto.UserDTO;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "User", fallbackFactory = UserClient.UserClientFallbackFactory.class)
public interface UserClient {

    // Microservice User : résumé utilisateur réel (inter-services)
    @GetMapping("/mic1/internal/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @Component
    class UserClientFallbackFactory implements FallbackFactory<UserClient> {
        @Override
        public UserClient create(Throwable cause) {
            return id -> {
                // Si User MS indisponible, on ne bloque pas (regle): utilisateur inconnu
                System.out.println("[FeignFallback] User MS indisponible, cause=" + cause.getClass().getSimpleName());
                if (cause instanceof FeignException.NotFound) {
                    // Le user n'existe pas (404)
                    throw (FeignException.NotFound) cause;
                }
                return UserDTO.builder()
                        .id(id)
                        .username("Utilisateur inconnu")
                        .email("")
                        .role(null)
                        .active(null)
                        .build();
            };
        }
    }
}
