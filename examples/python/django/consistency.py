from django.db import transaction


def apply_subscription_update(event):
    with transaction.atomic():
        subscription = Subscription.objects.select_for_update().get(
            external_id=event.resource_id
        )

        if not subscription.can_transition_to(event.payload["status"]):
            return

        subscription.status = event.payload["status"]
        subscription.save(update_fields=["status"])
