package esprit.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * @param subscriptionServiceUri défaut {@code lb://subscription-service} (Eureka). Si 503 : aucune instance.
     *                             Dev sans Eureka : {@code http://127.0.0.1:8099} via {@code gateway.subscription.uri}.
     */
    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            @Value("${gateway.subscription.uri:lb://subscription-service}") String subscriptionServiceUri) {
        return builder.routes()
                .route("consultation-microservice", r -> r.path("/apiConsultation/**")
                        .uri("lb://consultation-microservice"))
                // Avant la route catch-all /api/** → forum : tous les préfixes du MS abonnements
                .route("subscription-service", r -> r.path(
                                "/api/subscriptions/**",
                                "/api/promo/**",
                                "/api/notifications/**",
                                "/api/risk/**",
                                "/api/adjustment/**",
                                "/api/recommendations/**",
                                "/promo/**")
                        .uri(subscriptionServiceUri))
                .route("nephro-forum-api", r -> r.path("/api/**")
                        .filters(f -> f.dedupeResponseHeader(
                                "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                                "RETAIN_UNIQUE"))
                        .uri("lb://nephro-forum"))
                .route("nephro-forum-uploads", r -> r.path("/uploads/**")
                        .filters(f -> f.dedupeResponseHeader(
                                "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                                "RETAIN_UNIQUE"))
                        .uri("lb://nephro-forum"))
                .route("events", r -> r.path("/apiEvents/**")
                        .filters(f -> f.rewritePath("/apiEvents/(?<segment>.*)", "/${segment}"))
                        .uri("lb://events"))
                .route("events-uploads", r -> r.path("/apiUploads/**")
                        .filters(f -> f.rewritePath("/apiUploads/(?<segment>.*)", "/Events/uploads/${segment}"))
                        .uri("lb://events"))
                .route("dossiemedicale", r -> r
                        .path("/dossiemedicale/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://DOSSIEMEDICALE"))
                .build();
    }
}