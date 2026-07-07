import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AuthenticationExample {
    public interface WebhookAuthenticator {
        void authenticate(HttpServletRequest request);
    }

    public static final class BasicAuthWebhookAuthenticator implements WebhookAuthenticator {
        private final String expectedUsername;
        private final String expectedPassword;

        public BasicAuthWebhookAuthenticator(String expectedUsername, String expectedPassword) {
            this.expectedUsername = expectedUsername;
            this.expectedPassword = expectedPassword;
        }

        @Override
        public void authenticate(HttpServletRequest request) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }

            String decoded = new String(
                Base64.getDecoder().decode(authHeader.substring(6)),
                StandardCharsets.UTF_8
            );
            String[] parts = decoded.split(":", 2);

            if (parts.length != 2 ||
                !expectedUsername.equals(parts[0]) ||
                !expectedPassword.equals(parts[1])) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }
        }
    }
}
