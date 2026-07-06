import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ObservabilityExample {
    private static final Logger log = LoggerFactory.getLogger(ObservabilityExample.class);

    private final WebhookPipeline webhookPipeline;
    private final MeterRegistry meterRegistry;

    public ObservabilityExample(WebhookPipeline webhookPipeline, MeterRegistry meterRegistry) {
        this.webhookPipeline = webhookPipeline;
        this.meterRegistry = meterRegistry;
    }

    public void process(WebhookDelivery delivery) {
        long startedAt = System.nanoTime();

        log.info(
            "webhook delivery received provider={} deliveryId={} eventId={}",
            delivery.getProvider(),
            delivery.getId(),
            delivery.getEventId()
        );

        webhookPipeline.processDelivery(delivery);

        meterRegistry.timer("webhook.processing")
            .record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
        meterRegistry.counter("webhook.delivery.completed", "provider", delivery.getProvider()).increment();
    }
}
