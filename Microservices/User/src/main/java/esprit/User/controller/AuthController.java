package esprit.User.controller;

import org.springframework.beans.factory.annotation.Value;

import esprit.User.security.audit.LogAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import esprit.User.dto.FaceLoginRequest;
import esprit.User.dto.FaceRegisterRequest;
import esprit.User.dto.ForgotPasswordRequest;
import esprit.User.dto.LoginRequest;
import esprit.User.dto.LoginResponse;
import esprit.User.dto.RegisterRequest;
import esprit.User.dto.ResetPasswordRequest;
import esprit.User.entities.AuditLog;
import esprit.User.entities.Doctor;
import esprit.User.entities.Parent;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.repositories.AuditLogRepository;
import esprit.User.repositories.DoctorRepository;
import esprit.User.repositories.ParentRepository;
import esprit.User.repositories.UserRepository;
import esprit.User.security.CustomUserDetailsService;
import esprit.User.security.JwtService;
import esprit.User.services.EmailVerificationService;
import esprit.User.services.AdminSecurityNotificationService;
import esprit.User.services.ForgotPasswordService;
import esprit.User.services.FraudClientService;
import esprit.User.services.KeycloakService;
import esprit.User.security.SqlInjectionDetector;
import esprit.User.services.RecaptchaService;
import esprit.User.services.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    @Value("${global.message:Message local par defaut (user)})")
    private String configMessage;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int FACE_DESCRIPTOR_SIZE = 128;
    private static final double FACE_DISTANCE_THRESHOLD = 0.5d;
    private static final double FACE_COSINE_THRESHOLD = 0.6d;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final DoctorRepository doctorRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final ForgotPasswordService forgotPasswordService;
    private final EmailVerificationService emailVerificationService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final RecaptchaService recaptchaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditLogRepository auditLogRepository;
    private final KeycloakService keycloakService;
    private final FraudClientService fraudClientService;
    private final AdminSecurityNotificationService adminSecurityNotificationService;
    private final int maxFailedLoginAttempts;
    private final int lockMinutes;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          ParentRepository parentRepository,
                          DoctorRepository doctorRepository,
                          CustomUserDetailsService customUserDetailsService,
                          JwtService jwtService,
                          ForgotPasswordService forgotPasswordService,
                          EmailVerificationService emailVerificationService,
                          ObjectMapper objectMapper,
                          PasswordEncoder passwordEncoder,
                          RecaptchaService recaptchaService,
                          TokenBlacklistService tokenBlacklistService,
                          AuditLogRepository auditLogRepository,
                          KeycloakService keycloakService,
                          FraudClientService fraudClientService,
                          AdminSecurityNotificationService adminSecurityNotificationService,
                          @Value("${auth.failed-login.max-attempts:3}") int maxFailedLoginAttempts,
                          @Value("${auth.failed-login.lock-minutes:10}") int lockMinutes) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.parentRepository = parentRepository;
        this.doctorRepository = doctorRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.forgotPasswordService = forgotPasswordService;
        this.emailVerificationService = emailVerificationService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.recaptchaService = recaptchaService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.auditLogRepository = auditLogRepository;
        this.keycloakService = keycloakService;
        this.fraudClientService = fraudClientService;
        this.adminSecurityNotificationService = adminSecurityNotificationService;
        this.maxFailedLoginAttempts = Math.max(1, maxFailedLoginAttempts);
        this.lockMinutes = Math.max(1, lockMinutes);
    }

    @GetMapping("/config-message")
    public ResponseEntity<Map<String, String>> getConfigMessage() {
        return ResponseEntity.ok(Map.of("message", configMessage));
    }

    @PostMapping({"/auth/register"})
    public ResponseEntity<?> register(@RequestBody(required = false) RegisterRequest request, HttpServletRequest http) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le corps de la requete est vide"));
        }

        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();
        String captchaToken = request.getCaptchaToken();

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le username est obligatoire"));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'email est obligatoire"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe est obligatoire"));
        }

        String remoteIp = http == null ? null : http.getRemoteAddr();
        RecaptchaService.VerificationResult captcha = recaptchaService.verify(captchaToken, remoteIp);
        if (!captcha.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", captcha.error() == null ? "Captcha invalide." : captcha.error()));
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username deja utilise"));
        }
        if (userRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email deja utilise"));
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PARENT);
        user.setActive(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Synchroniser avec Keycloak (non-bloquant en cas d'indisponibilité)
        try {
            keycloakService.createUserInKeycloak(username.trim(), email.trim().toLowerCase(), password, Role.PARENT);
        } catch (Exception ex) {
            log.warn("[Keycloak] Sync user '{}' echouee (non bloquant): {}", username, ex.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Inscription reussie"));
    }

    @PostMapping({"/login", "/auth/login", "/mic1/login"})
    @LogAction(action = "LOGIN")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le corps de la requete est vide"));
        }

        String usernameOrEmail = request.getUsernameOrEmail();
        String password = request.getPassword();

        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "L'email ou le username est obligatoire"));
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le mot de passe est obligatoire"));
        }

        if (SqlInjectionDetector.looksMalicious(usernameOrEmail, password)) {
            log.warn("[Security] Motifs d'injection SQL détectés sur /login (IP={})",
                    httpRequest != null ? httpRequest.getRemoteAddr() : "?");
            try {
                if (httpRequest != null) {
                    adminSecurityNotificationService.recordSqlInjectionLoginAttempt(httpRequest, usernameOrEmail);
                }
            } catch (Exception ex) {
                log.error("[Security] Enregistrement alerte admin échoué: {}", ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Identifiants non valides."));
        }

        try {
            User user = userRepository.findFirstByUsernameOrEmail(usernameOrEmail)
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

            // Block login if the account is currently locked due to failed attempts
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lockedUntil = user.getLoginLockedUntil();
            if (lockedUntil != null && lockedUntil.isAfter(now)) {
                long seconds = Math.max(1, Duration.between(now, lockedUntil).getSeconds());
                long minutes = (long) Math.ceil(seconds / 60.0);
                return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                        "error", "Compte bloque. Reessayez dans " + minutes + " minute(s)."
                ));
            }
            // Deblocage automatique : si le delai est depasse, reinitialiser les compteurs
            if (lockedUntil != null && !lockedUntil.isAfter(now)) {
                user.setLoginLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }



            // Mimic Spring Security's DisabledException message, but with a clear response
            if (!Boolean.TRUE.equals(user.getEnabled()) || !Boolean.TRUE.equals(user.getActive())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Compte non active ou desactive. Verifiez votre email."));
            }

            // Check password (we still authenticate later to keep the security pipeline consistent)
            if (!passwordEncoder.matches(password, user.getPassword())) {
                int current = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
                int next = current + 1;
                user.setFailedLoginAttempts(next);

                if (next >= maxFailedLoginAttempts) {
                    user.setLoginLockedUntil(now.plusMinutes(lockMinutes));
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                            "error", "Compte bloque pour " + lockMinutes + " minute(s) apres trop de tentatives."
                    ));
                }

                userRepository.save(user);
                int remaining = Math.max(0, maxFailedLoginAttempts - next);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "Mot de passe incorrect. Il vous reste " + remaining + " tentative(s)."
                ));
            }

            // Successful login: reset counters
            user.setFailedLoginAttempts(0);
            user.setLoginLockedUntil(null);
            userRepository.save(user);

            // Call FraudClientService for score check
            String ipAddress = httpRequest.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = httpRequest.getRemoteAddr();
            }
            String userAgent = httpRequest.getHeader("User-Agent");
            fraudClientService.analyzeAction(user.getId(), "LOGIN", ipAddress, userAgent != null ? userAgent : "Unknown");

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
            Long parentId = null;
            Long doctorId = null;
            if (user.getRole() == Role.PARENT) {
                Parent parent = parentRepository.findByUserUsername(user.getUsername()).orElse(null);
                if (parent != null) {
                    parentId = parent.getId();
                }
            } else if (user.getRole() == Role.DOCTOR) {
                Doctor doctor = doctorRepository.findByUserUsername(user.getUsername()).orElse(null);
                if (doctor != null) {
                    doctorId = doctor.getId();
                }
            }
            String token = jwtService.generateToken(
                    userDetails,
                    jwtClaimsForUser(user, parentId, doctorId)
            );

            return ResponseEntity.ok(new LoginResponse(
                    "Connexion reussie",
                    token,
                    user.getRole().name(),
                    user.getUsername(),
                    user.getEmail(),
                    parentId,
                    doctorId,
                    user.getId()
            ));

        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Compte non active ou desactive. Verifiez votre email."));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email/username ou mot de passe incorrect"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Une erreur est survenue lors de la connexion: " + ex.getMessage()));
        }
    }


    @PostMapping({"/mic1/google-login", "/auth/google-login"})
    @LogAction(action = "GOOGLE_LOGIN")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Aucun compte trouvé avec cet email", "found", false));
        }

        // Generate JWT token for this user (no password check needed — Google already verified)
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Long parentId = null;
        Long doctorId = null;
        if (user.getRole() == Role.PARENT) {
            Parent parent = parentRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (parent != null) parentId = parent.getId();
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (doctor != null) doctorId = doctor.getId();
        }
        String token = jwtService.generateToken(
                userDetails,
                jwtClaimsForUser(user, parentId, doctorId)
        );

        return ResponseEntity.ok(new LoginResponse(
                "Connexion Google reussie",
                token,
                user.getRole().name(),
                user.getUsername(),
                user.getEmail(),
                parentId,
                doctorId,
                user.getId()
        ));
    }

    @PostMapping({"/auth/forgot-password", "/mic1/forgot-password"})
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'email est obligatoire"));
        }

        try {
            forgotPasswordService.requestPasswordReset(request.getEmail());
        } catch (RuntimeException ignored) {
        }

        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email, un lien de reinitialisation a ete envoye."
        ));
    }

    @PostMapping({"/auth/reset-password", "/mic1/reset-password"})
    @LogAction(action = "RESET_PASSWORD")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le token est obligatoire"));
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nouveau mot de passe est obligatoire"));
        }

        try {
            forgotPasswordService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Mot de passe reinitialise avec succes. Vous pouvez maintenant vous connecter."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping({"/auth/verify-reset-token", "/mic1/verify-reset-token"})
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {
        boolean isValid = forgotPasswordService.isTokenValid(token);
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("valid", false, "error", "Token invalide ou expire"));
    }

    @GetMapping({"/auth/verify-email", "/mic1/verify-email"})
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            String message = emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "Erreur de verification" : e.getMessage();
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if ("Compte deja active".equals(message)) {
                status = HttpStatus.CONFLICT;
            }
            return ResponseEntity.status(status).body(Map.of("error", message));
        }
    }

    @PostMapping({"/mic/face-register", "/mic1/face-register"})
    public ResponseEntity<?> registerFace(Authentication authentication, @RequestBody FaceRegisterRequest request) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise"));
        }

        List<Double> descriptor = request == null ? null : request.getDescriptor();
        if (!isValidDescriptor(descriptor)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le face descriptor doit contenir exactement 128 valeurs numeriques"));
        }

        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Utilisateur introuvable"));
        }

        try {
            user.setFaceDescriptor(objectMapper.writeValueAsString(descriptor));
            user.setFaceIdEnabled(true);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Face descriptor enregistre avec succes"));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Impossible de serialiser le face descriptor"));
        }
    }

    @PostMapping({"/mic/face-login", "/mic1/face-login"})
    @LogAction(action = "FACE_LOGIN")
    public ResponseEntity<?> loginWithFace(@RequestBody(required = false) FaceLoginRequest request) {
        List<Double> descriptor = request == null ? null : request.getDescriptor();
        if (descriptor == null) {
            log.warn("Face login rejected: descriptor is null (request null: {})", request == null);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        if (descriptor.size() != FACE_DESCRIPTOR_SIZE) {
            log.warn("Face login rejected: invalid descriptor size = {}", descriptor.size());
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        if (!isValidDescriptor(descriptor)) {
            log.warn("Face login rejected: descriptor contains null/non-finite values");
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        Optional<UserMatch> bestMatch = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getFaceIdEnabled()))
                .filter(user -> user.getFaceDescriptor() != null && !user.getFaceDescriptor().isBlank())
                .map(user -> mapToUserMatch(user, descriptor))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.comparingDouble(UserMatch::distance));

        if (bestMatch.isEmpty() || bestMatch.get().distance() >= FACE_DISTANCE_THRESHOLD) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        User user = bestMatch.get().user();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Long parentId = null;
        Long doctorId = null;
        if (user.getRole() == Role.PARENT) {
            Parent parent = parentRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (parent != null) {
                parentId = parent.getId();
            }
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (doctor != null) {
                doctorId = doctor.getId();
            }
        }
        String token = jwtService.generateToken(
                userDetails,
                jwtClaimsForUser(user, parentId, doctorId)
        );

        LoginResponse loginResponse = new LoginResponse(
                token,
                user.getRole().name(),
                user.getUsername(),
                user.getEmail(),
                parentId,
                doctorId,
                user.getId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("recognized", true);
        response.put("distance", bestMatch.get().distance());
        response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
        response.put("auth", loginResponse);
        response.put("token", loginResponse.getToken());
        response.put("role", loginResponse.getRole());
        response.put("username", loginResponse.getUsername());
        response.put("email", loginResponse.getEmail());
        response.put("parentId", loginResponse.getParentId());
        response.put("doctorId", loginResponse.getDoctorId());
        response.put("id", loginResponse.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/face-login")
    @LogAction(action = "FACE_LOGIN")
    public ResponseEntity<?> faceLogin(@RequestBody(required = false) FaceLoginRequest request) {
        try {
        List<Double> descriptor = request == null ? null : request.getDescriptor();
        if (!isValidDescriptor(descriptor)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        Optional<UserMatchCosine> bestMatch = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getFaceIdEnabled()))
                .filter(user -> user.getFaceDescriptor() != null && !user.getFaceDescriptor().isBlank())
                .map(user -> mapToUserMatchCosine(user, descriptor))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingDouble(UserMatchCosine::similarity));

        if (bestMatch.isEmpty() || bestMatch.get().similarity() <= FACE_COSINE_THRESHOLD) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Utilisateur non reconnu"
            ));
        }

        User user = bestMatch.get().user();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        Long parentId = null;
        Long doctorId = null;
        if (user.getRole() == Role.PARENT) {
            Parent parent = parentRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (parent != null) {
                parentId = parent.getId();
            }
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserUsername(user.getUsername()).orElse(null);
            if (doctor != null) {
                doctorId = doctor.getId();
            }
        }
        String token = jwtService.generateToken(
                userDetails,
                jwtClaimsForUser(user, parentId, doctorId)
        );

        LoginResponse loginResponse = new LoginResponse(
                token,
                user.getRole().name(),
                user.getUsername(),
                user.getEmail(),
                parentId,
                doctorId,
                user.getId()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("token", loginResponse.getToken());
        body.put("role", loginResponse.getRole());
        body.put("username", loginResponse.getUsername());
        body.put("email", loginResponse.getEmail());
        body.put("id", loginResponse.getId());
        body.put("parentId", loginResponse.getParentId());
        body.put("doctorId", loginResponse.getDoctorId());
        body.put("userId", loginResponse.getId());
        return ResponseEntity.ok(body);
        } catch (Exception ex) {
            log.error("faceLogin failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur serveur lors de la connexion par visage. Si le problème persiste, reconnectez-vous par email/mot de passe ou contactez le support."
                    ));
        }
    }

    @PostMapping({"/auth/logout", "/mic1/logout"})
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing Bearer token"));
        }
        String token = authHeader.substring(7);

        // Extract user identity BEFORE any cleanup/blacklist so we don't lose it.
        Long userId = null;
        String username = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            if (auth.getPrincipal() instanceof esprit.User.security.CustomUserDetails cud) {
                userId = cud.getId();
            }
        }

        // Fallback: in case SecurityContext is empty, derive username from the JWT itself.
        if (username == null || username.isBlank()) {
            try {
                username = jwtService.extractUsername(token);
            } catch (Exception ignored) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
            }
        }

        if (userId == null && username != null && !username.isBlank()) {
            User u = userRepository.findByUsername(username);
            if (u != null) {
                userId = u.getId();
            }
        }

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Persist the audit log synchronously BEFORE blacklist/session cleanup.
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .username(username)
                .action("LOGOUT")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details(null)
                .build());

        tokenBlacklistService.blacklistToken(token);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private boolean isValidDescriptor(List<Double> descriptor) {
        return descriptor != null
                && descriptor.size() == FACE_DESCRIPTOR_SIZE
                && descriptor.stream().allMatch(value -> value != null && Double.isFinite(value));
    }

    private Optional<UserMatch> mapToUserMatch(User user, List<Double> incomingDescriptor) {
        try {
            List<Double> savedDescriptor = objectMapper.readValue(
                    user.getFaceDescriptor(),
                    new TypeReference<List<Double>>() {}
            );

            if (!isValidDescriptor(savedDescriptor)) {
                return Optional.empty();
            }

            double distance = euclideanDistance(incomingDescriptor, savedDescriptor);
            return Optional.of(new UserMatch(user, distance));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<UserMatchCosine> mapToUserMatchCosine(User user, List<Double> incomingDescriptor) {
        try {
            List<Double> savedDescriptor = objectMapper.readValue(
                    user.getFaceDescriptor(),
                    new TypeReference<List<Double>>() {}
            );

            if (!isValidDescriptor(savedDescriptor)) {
                return Optional.empty();
            }

            double similarity = cosineSimilarity(incomingDescriptor, savedDescriptor);
            return Optional.of(new UserMatchCosine(user, similarity));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0d;
        double normA = 0d;
        double normB = 0d;
        for (int i = 0; i < FACE_DESCRIPTOR_SIZE; i++) {
            double av = a.get(i);
            double bv = b.get(i);
            dot += av * bv;
            normA += av * av;
            normB += bv * bv;
        }
        if (normA == 0d || normB == 0d) {
            return -1d;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double euclideanDistance(List<Double> a, List<Double> b) {
        double sum = 0d;
        for (int i = 0; i < FACE_DESCRIPTOR_SIZE; i++) {
            double diff = a.get(i) - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private Map<String, Object> jwtClaimsForUser(User user, Long parentId, Long doctorId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("id", user.getId());
        if (parentId != null) {
            claims.put("parentId", parentId);
        }
        if (doctorId != null) {
            claims.put("doctorId", doctorId);
        }
        return claims;
    }

    private record UserMatch(User user, double distance) {
    }

    private record UserMatchCosine(User user, double similarity) {
    }
}
