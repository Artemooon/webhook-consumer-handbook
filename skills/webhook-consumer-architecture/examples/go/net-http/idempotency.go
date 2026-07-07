package examples

import (
	"log/slog"
	"net/http"
)

func HandleWebhookRequest(
	w http.ResponseWriter,
	r *http.Request,
	logger *slog.Logger,
	cache WebhookEventCache,
	mapper WebhookEventMapper,
	pipeline WebhookPipeline,
	body map[string]any,
) {
	content, _ := body["content"].(map[string]any)
	eventName, _ := body["event_type"].(string)
	eventID, _ := body["id"].(string)
	eventType := mapper.GetEventType(eventName)

	if eventID == "" || eventType == "" {
		w.WriteHeader(http.StatusOK)
		return
	}

	if cache.Exists(eventID) {
		logger.Info("duplicate webhook event", "event_id", eventID)
		w.WriteHeader(http.StatusOK)
		return
	}

	event := NormalizeSubscriptionEvent(
		ProviderSubscriptionPayload{
			ID:         content["id"].(string),
			CustomerID: content["customer_id"].(string),
			PlanID:     content["plan_id"].(string),
			Status:     content["status"].(string),
		},
		eventID,
		eventType,
	)

	defer cache.Put(eventID)
	_ = pipeline.Process(r.Context(), event)
}
