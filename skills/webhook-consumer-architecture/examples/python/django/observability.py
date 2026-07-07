import logging
import time

logger = logging.getLogger("webhooks")


def process_with_observability(delivery):
    started_at = time.monotonic()

    logger.info(
        "webhook.delivery.received",
        extra={
            "provider": delivery.provider,
            "delivery_id": delivery.id,
            "event_id": delivery.event_id,
        },
    )

    webhook_pipeline.process_delivery(delivery)

    metrics.timing("webhook.processing.ms", (time.monotonic() - started_at) * 1000)
    metrics.increment("webhook.delivery.completed", tags={"provider": delivery.provider})
