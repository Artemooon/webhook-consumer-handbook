import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public final class ValidationExample {
    public record ProviderSubscriptionPayload(
        @NotBlank String id,
        @NotBlank String customerId,
        @NotBlank String planId,
        @NotBlank String status
    ) {}

    public record NormalizedWebhookEvent(
        String provider,
        String eventId,
        String eventType,
        String resourceId,
        Map<String, Object> payload
    ) {}

    public static NormalizedWebhookEvent normalize(
        ProviderSubscriptionPayload payload,
        String eventId,
        String eventType
    ) {
        return new NormalizedWebhookEvent(
            "chargebee",
            eventId,
            eventType,
            payload.id(),
            Map.of(
                "customer_id", payload.customerId(),
                "plan_id", payload.planId(),
                "status", payload.status()
            )
        );
    }
}
