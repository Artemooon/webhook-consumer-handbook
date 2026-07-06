import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ReliabilityExample {
    private final WebhookDeliveryService deliveryService;
    private final WebhookJobPublisher jobPublisher;
    private final AuthenticationExample.WebhookAuthenticator authenticator;

    public ReliabilityExample(
        WebhookDeliveryService deliveryService,
        WebhookJobPublisher jobPublisher,
        AuthenticationExample.WebhookAuthenticator authenticator
    ) {
        this.deliveryService = deliveryService;
        this.jobPublisher = jobPublisher;
        this.authenticator = authenticator;
    }

    @PostMapping("/webhooks/chargebee")
    public ResponseEntity<Void> receive(HttpServletRequest request, @RequestBody String rawBody) {
        authenticator.authenticate(request);

        WebhookDelivery delivery = deliveryService.register("chargebee", request, rawBody);
        jobPublisher.publish(delivery.getId());

        return ResponseEntity.accepted().build();
    }
}
