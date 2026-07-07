import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class IdempotencyExample {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyExample.class);

    private final WebhookEventCache webhookEventCache;
    private final WebhookEventMapper webhookEventMapper;
    private final WebhookPipeline webhookPipeline;

    public IdempotencyExample(
        WebhookEventCache webhookEventCache,
        WebhookEventMapper webhookEventMapper,
        WebhookPipeline webhookPipeline
    ) {
        this.webhookEventCache = webhookEventCache;
        this.webhookEventMapper = webhookEventMapper;
        this.webhookPipeline = webhookPipeline;
    }

    public void handle(Map<String, Object> body) {
        Map<String, Object> content = cast(body.get("content"));
        String eventName = (String) body.get("event_type");
        String eventId = (String) body.get("id");
        String eventType = webhookEventMapper.getEventType(eventName);

        if (eventId == null || eventType == null) {
            throw new ResponseStatusException(HttpStatus.OK, "Unhandled event");
        }

        if (webhookEventCache.exists(eventId)) {
            log.info("Duplicate webhook event eventId={}", eventId);
            throw new ResponseStatusException(HttpStatus.OK, "Duplicated event");
        }

        ValidationExample.NormalizedWebhookEvent event =
            ValidationExample.normalize(mapPayload(content), eventId, eventType);

        try {
            webhookPipeline.process(event);
        } finally {
            webhookEventCache.put(eventId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object value) {
        return (Map<String, Object>) value;
    }

    private ValidationExample.ProviderSubscriptionPayload mapPayload(Map<String, Object> content) {
        return new ValidationExample.ProviderSubscriptionPayload(
            (String) content.get("id"),
            (String) content.get("customer_id"),
            (String) content.get("plan_id"),
            (String) content.get("status")
        );
    }
}
