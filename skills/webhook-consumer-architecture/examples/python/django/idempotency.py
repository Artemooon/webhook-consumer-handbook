import logging

from rest_framework.status import HTTP_200_OK

logger = logging.getLogger(__name__)


def handle_webhook_request(request):
    data = request.data
    content = data.get("content")
    event_name = data.get("event_type")
    event_id = data.get("id")
    event_type = webhook_event_mapper.get_event_type(event_name)

    if not event_id or not event_type:
        raise ViewException("Unhandled event", status_code=HTTP_200_OK)

    if webhook_event_cache.get_cache(event_id):
        logger.info("Duplicate webhook event", extra={"event_id": event_id})
        raise ViewException("Duplicated event", status_code=HTTP_200_OK)

    normalized_event = normalize_subscription_event(content, event_id, event_type)

    try:
        webhook_pipeline.process(normalized_event)
    finally:
        webhook_event_cache.set_cache(event_id)
