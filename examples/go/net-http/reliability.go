package examples

import (
	"io"
	"net/http"
)

type WebhookHandler struct {
	DeliveryService DeliveryService
	Queue           JobQueue
}

func (h *WebhookHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	rawBody, err := io.ReadAll(r.Body)
	if err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	deliveryID, err := h.DeliveryService.Register(r.Context(), "chargebee", r.Header, rawBody)
	if err != nil {
		http.Error(w, "failed to register delivery", http.StatusInternalServerError)
		return
	}

	h.Queue.Publish(deliveryID)
	w.WriteHeader(http.StatusAccepted)
}
