package examples

import (
	"context"
	"net/http"
)

type WebhookAdapter interface {
	Normalize(r *http.Request, deliveryID string) (NormalizedWebhookEvent, error)
}

type WebhookPipeline interface {
	Process(ctx context.Context, event NormalizedWebhookEvent) error
	ProcessDelivery(ctx context.Context, delivery Delivery) error
}
