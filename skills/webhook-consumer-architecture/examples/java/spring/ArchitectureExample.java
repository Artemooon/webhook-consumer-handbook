public final class ArchitectureExample {
    public interface WebhookAdapter {
        ValidationExample.NormalizedWebhookEvent normalize(Object request, String deliveryId);
    }

    public interface WebhookPipeline {
        void process(ValidationExample.NormalizedWebhookEvent event);

        void processDelivery(WebhookDelivery delivery);
    }
}
