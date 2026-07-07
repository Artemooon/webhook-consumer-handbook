public final class RecoverabilityExample {
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookPipeline webhookPipeline;
    private final RetryScheduler retryScheduler;

    public RecoverabilityExample(
        WebhookDeliveryRepository deliveryRepository,
        WebhookPipeline webhookPipeline,
        RetryScheduler retryScheduler
    ) {
        this.deliveryRepository = deliveryRepository;
        this.webhookPipeline = webhookPipeline;
        this.retryScheduler = retryScheduler;
    }

    public void processDelivery(Long deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();

        try {
            webhookPipeline.processDelivery(delivery);
            delivery.markCompleted();
        } catch (TemporaryWebhookException ex) {
            delivery.markRetryable(ex.getMessage());
            retryScheduler.schedule(delivery.getId());
        } catch (Exception ex) {
            delivery.markFailed(ex.getMessage());
            throw ex;
        }
    }
}
