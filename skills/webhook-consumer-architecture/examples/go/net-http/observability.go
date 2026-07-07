package examples

import (
	"context"
	"time"
)

func (w *Worker) ProcessWithObservability(ctx context.Context, deliveryID string) error {
	startedAt := time.Now()

	delivery, err := w.DeliveryStore.Get(ctx, deliveryID)
	if err != nil {
		return err
	}

	w.Logger.Info("webhook delivery received",
		"provider", delivery.Provider,
		"delivery_id", delivery.ID,
		"event_id", delivery.EventID,
	)

	err = w.Pipeline.ProcessDelivery(ctx, delivery)
	w.Metrics.ObserveDuration("webhook.processing.ms", time.Since(startedAt))

	if err == nil {
		w.Metrics.Increment("webhook.delivery.completed", delivery.Provider)
	}

	return err
}
