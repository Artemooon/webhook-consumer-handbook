import org.springframework.transaction.annotation.Transactional;

public final class ConsistencyExample {
    private final SubscriptionRepository subscriptionRepository;

    public ConsistencyExample(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void applyUpdate(ValidationExample.NormalizedWebhookEvent event) {
        Subscription subscription = subscriptionRepository
            .findByExternalIdForUpdate(event.resourceId())
            .orElseThrow();

        String nextStatus = (String) event.payload().get("status");
        if (!subscription.canTransitionTo(nextStatus)) {
            return;
        }

        subscription.setStatus(nextStatus);
    }
}
