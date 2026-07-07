from datetime import datetime

from pydantic import BaseModel


class ProviderSubscriptionPayload(BaseModel):
    id: str
    customer_id: str
    plan_id: str
    status: str
    current_term_end: datetime | None = None


class NormalizedWebhookEvent(BaseModel):
    provider: str
    event_id: str
    event_type: str
    resource_id: str
    payload: dict


def normalize_subscription_event(data: dict, event_id: str, event_type: str) -> NormalizedWebhookEvent:
    payload = ProviderSubscriptionPayload.model_validate(data)

    return NormalizedWebhookEvent(
        provider="chargebee",
        event_id=event_id,
        event_type=event_type,
        resource_id=payload.id,
        payload=payload.model_dump(),
    )
