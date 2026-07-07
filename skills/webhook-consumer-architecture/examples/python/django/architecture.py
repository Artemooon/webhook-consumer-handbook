from pydantic import BaseModel


class NormalizedWebhookEvent(BaseModel):
    provider: str
    delivery_id: str
    event_id: str
    event_type: str
    resource_id: str
    payload: dict


class ProviderWebhookAdapter:
    provider = None

    def normalize(self, request, delivery_id: str) -> NormalizedWebhookEvent:
        raise NotImplementedError
