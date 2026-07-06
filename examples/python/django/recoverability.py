def process_delivery(delivery_id: int) -> None:
    delivery = WebhookDelivery.objects.get(id=delivery_id)

    try:
        webhook_pipeline.process_delivery(delivery)
    except TemporaryWebhookError as exc:
        delivery.mark_retryable(str(exc))
        retry_webhook_delivery.apply_async(args=[delivery.id], countdown=60)
    except Exception as exc:
        delivery.mark_failed(str(exc))
        raise
    else:
        delivery.mark_completed()
