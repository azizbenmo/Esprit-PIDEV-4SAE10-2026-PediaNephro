package com.pedianephro.subscription.client;

import com.pedianephro.subscription.client.dto.UserPublicSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign vers le microservice utilisateur (Eureka : {@code PEDIANEPHRO} par défaut).
 * Endpoint interne autorisé sans JWT : {@code /mic1/internal/users/**}.
 */
@FeignClient(
        name = "${subscription.integration.user-service-name:PEDIANEPHRO}",
        contextId = "subscriptionUserServiceClient"
)
public interface UserServiceClient {

    @GetMapping("/mic1/internal/users/{id}")
    UserPublicSummaryDto getUserById(@PathVariable("id") Long id);
}
