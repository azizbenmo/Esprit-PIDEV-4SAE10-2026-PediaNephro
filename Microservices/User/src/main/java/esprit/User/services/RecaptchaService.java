package esprit.User.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestClient restClient;
    private final String secret;
    private final boolean enabled;
    private final double minimumScore;

    public RecaptchaService(
            @Value("${recaptcha.secret:}") String secret,
            @Value("${recaptcha.enabled:true}") boolean enabled,
            @Value("${recaptcha.minimum-score:0.5}") double minimumScore
    ) {
        this.restClient = RestClient.create();
        this.secret = secret;
        this.enabled = enabled;
        this.minimumScore = minimumScore;
    }

    public VerificationResult verify(String captchaToken, String remoteIp) {
        if (!enabled) {
            return VerificationResult.ok();
        }
        if (secret == null || secret.isBlank()) {
            log.warn("reCAPTCHA is enabled but recaptcha.secret is empty; rejecting verification.");
            return VerificationResult.fail("Configuration reCAPTCHA manquante (secret).");
        }
        if (captchaToken == null || captchaToken.isBlank()) {
            return VerificationResult.fail("captchaToken est obligatoire.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("response", captchaToken);
        if (remoteIp != null && !remoteIp.isBlank()) {
            form.add("remoteip", remoteIp);
        }

        try {
            GoogleResponse response = restClient.post()
                    .uri(VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleResponse.class);

            if (response == null) {
                return VerificationResult.fail("Verification reCAPTCHA indisponible.");
            }
            if (!response.isSuccess()) {
                return VerificationResult.fail("Captcha invalide.");
            }
            if (response.getScore() != null && response.getScore() < minimumScore) {
                return VerificationResult.fail("Score reCAPTCHA trop faible.");
            }

            return VerificationResult.ok();
        } catch (Exception e) {
            log.warn("reCAPTCHA verification failed due to an exception: {}", e.toString());
            return VerificationResult.fail("Erreur de verification reCAPTCHA.");
        }
    }

    public record VerificationResult(boolean success, String error) {
        public static VerificationResult ok() {
            return new VerificationResult(true, null);
        }

        public static VerificationResult fail(String error) {
            return new VerificationResult(false, error);
        }
    }

    public static class GoogleResponse {
        private boolean success;

        @JsonProperty("challenge_ts")
        private String challengeTs;

        private String hostname;
        private Double score;
        private String action;

        @JsonProperty("error-codes")
        private List<String> errorCodes;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getChallengeTs() {
            return challengeTs;
        }

        public void setChallengeTs(String challengeTs) {
            this.challengeTs = challengeTs;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public List<String> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(List<String> errorCodes) {
            this.errorCodes = errorCodes;
        }
    }
}
