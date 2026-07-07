package examples

type ProviderSubscriptionPayload struct {
	ID         string `json:"id"`
	CustomerID string `json:"customer_id"`
	PlanID     string `json:"plan_id"`
	Status     string `json:"status"`
}

type NormalizedWebhookEvent struct {
	Provider   string
	EventID    string
	EventType  string
	ResourceID string
	Payload    map[string]any
}

func NormalizeSubscriptionEvent(payload ProviderSubscriptionPayload, eventID, eventType string) NormalizedWebhookEvent {
	return NormalizedWebhookEvent{
		Provider:   "chargebee",
		EventID:    eventID,
		EventType:  eventType,
		ResourceID: payload.ID,
		Payload: map[string]any{
			"customer_id": payload.CustomerID,
			"plan_id":     payload.PlanID,
			"status":      payload.Status,
		},
	}
}
