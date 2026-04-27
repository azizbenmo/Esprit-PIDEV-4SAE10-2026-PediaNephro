package esprit.User.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    // ─────────────────────────────────────────────────────────────────
    // Chaîne 1 (Order 1) : JWT maison — gère toutes les routes existantes
    // Cette chaîne reste exactement comme avant, rien n'est supprimé.
    // ─────────────────────────────────────────────────────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/auth/**",
                        "/login",
                        "/mic1/**",
                        "/mic/**",
                        "/admin/**",
                        "/doctor/**",
                        "/parent/**",
                        "/patient/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .xssProtection(xss -> xss.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.deny())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Routes publiques (authentification) ──────────────
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/login",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/auth/verify-reset-token",
                                "/auth/verify-email",
                                "/auth/google-login"
                        ).permitAll()
                        .requestMatchers(
                                "/mic1/admins/register",
                                "/mic1/doctors/register",
                                "/mic1/parents/register",
                                "/mic1/patients/register",
                                "/mic1/login",
                                "/mic/face-login",
                                "/mic1/face-login",
                                "/mic1/forgot-password",
                                "/mic1/reset-password",
                                "/mic1/verify-reset-token",
                                "/mic1/verify-email",
                                "/mic1/utilisateurs",
                                "/mic1/parents",
                                "/mic1/google-login",
                                "/mic1/activity/log",
                                "/mic1/internal/users/**",
                                "/mic1/doctors/public/**",
                                "/mic1/patients/public/**",
                                "/doctor/public/**",
                                "/mic1/patients/sync-from-dossier"
                        ).permitAll()

                        // ── Routes protégées par rôle (JWT maison) ───────────
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/parent/**").hasRole("PARENT")
                        .requestMatchers("/patient/**").hasAnyRole("PARENT", "PATIENT")
                        .requestMatchers("/mic1/admins/**").hasRole("ADMIN")
                        .requestMatchers("/mic1/doctors/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/mic1/parents/**").hasRole("PARENT")
                        .requestMatchers("/mic1/patients/**").hasAnyRole("PATIENT", "PARENT", "DOCTOR", "ADMIN")
                        .requestMatchers("/mic1/profile", "/mic1/activity/history")
                            .hasAnyRole("PARENT", "DOCTOR", "ADMIN", "PATIENT")
                        .requestMatchers("/mic1/utilisateurs/**").hasRole("ADMIN")
                        .requestMatchers("/mic1/activity/admin/history/**").hasRole("ADMIN")
                        .requestMatchers("/mic1/admin/security-notifications/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                // Filtre JWT maison appliqué en premier
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Chaîne 2 (Order 2) : Keycloak OAuth2 — routes /api/**
    // Valide les tokens émis par Keycloak (issuer-uri configuré dans properties)
    // ─────────────────────────────────────────────────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain keycloakFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques Keycloak
                        .requestMatchers("/api/auth/**").permitAll()

                        // Routes protégées par rôle Keycloak
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/parent/**").hasRole("PARENT")

                        .anyRequest().authenticated()
                )
                // Validation des tokens Keycloak via OAuth2 Resource Server
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
                );

        return http.build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Convertisseur Keycloak JWT → Spring Security Authorities
    // Lit les rôles depuis le claim imbriqué "realm_access" → "roles"
    // Le JwtGrantedAuthoritiesConverter standard ne supporte PAS les
    // claims imbriqués, donc on utilise un converter custom.
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public JwtAuthenticationConverter keycloakJwtConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        // Converter custom qui extrait les rôles du claim imbriqué realm_access.roles
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return jwtConverter;
    }

    /**
     * Converter custom pour extraire les rôles Keycloak depuis le claim
     * imbriqué realm_access.roles et les préfixer avec "ROLE_" pour
     * que Spring Security les reconnaisse correctement.
     *
     * Exemple de structure du token Keycloak :
     * {
     *   "realm_access": {
     *     "roles": ["ADMIN", "DOCTOR", "PARENT", "offline_access"]
     *   }
     * }
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Récupérer le claim "realm_access"
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
                return Collections.emptyList();
            }

            // Récupérer la liste "roles" depuis realm_access
            Object rolesObj = realmAccess.get("roles");
            if (!(rolesObj instanceof List)) {
                return Collections.emptyList();
            }

            List<String> roles = (List<String>) rolesObj;

            // Filtrer les rôles techniques Keycloak et préfixer avec "ROLE_"
            return roles.stream()
                    .filter(role -> role.equals("ADMIN") || role.equals("DOCTOR")
                            || role.equals("PARENT") || role.equals("PATIENT"))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Beans partagés
    // ─────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
