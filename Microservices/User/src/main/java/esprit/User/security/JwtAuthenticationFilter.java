package esprit.User.security;

import esprit.User.services.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenLower = token.toLowerCase(Locale.ROOT);
        if ("null".equals(tokenLower) || "undefined".equals(tokenLower)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username;
        String tokenRole = null;
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            username = jwtService.extractUsername(token);
            tokenRole = jwtService.extractRole(token);
        } catch (RuntimeException ex) {
            // Ignore malformed/expired token and continue as anonymous.
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                    if (tokenRole != null && !tokenRole.isBlank()) {
                        String normalizedTokenRole = "ROLE_" + tokenRole.toUpperCase(Locale.ROOT);
                        boolean roleMatchesUser = userDetails.getAuthorities().stream()
                                .anyMatch(a -> normalizedTokenRole.equals(a.getAuthority()));
                        if (!roleMatchesUser) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                        authorities = List.of(new SimpleGrantedAuthority(normalizedTokenRole));
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (RuntimeException ignored) {
                // Invalid user/token state: keep request unauthenticated.
            }
        }

        filterChain.doFilter(request, response);
    }
}
