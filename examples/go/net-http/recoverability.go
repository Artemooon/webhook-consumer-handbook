package examples

import (
	"context"
	"errors"
	"time"
)

func (w *Worker) ProcessDelivery(ctx context.Context, deliveryID string) error {
	delivery, err := w.DeliveryStore.Get(ctx, deliveryID)
	if err != nil {
		return err
	}

	if err := w.Pipeline.ProcessDelivery(ctx, delivery); err != nil {
		if errors.Is(err, ErrTemporaryWebhookFailure) {
			_ = w.DeliveryStore.MarkRetryable(ctx, deliveryID, err.Error())
			return w.RetryQueue.Schedule(ctx, deliveryID, time.Minute)
		}

		_ = w.DeliveryStore.MarkFailed(ctx, deliveryID, err.Error())
		return err
	}

	return w.DeliveryStore.MarkCompleted(ctx, deliveryID)
}
