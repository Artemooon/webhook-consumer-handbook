package examples

import "context"

func (s *SubscriptionService) ApplyUpdate(ctx context.Context, event NormalizedWebhookEvent) error {
	tx, err := s.DB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	subscription, err := s.Repo.GetForUpdate(ctx, tx, event.ResourceID)
	if err != nil {
		return err
	}

	nextStatus := event.Payload["status"].(string)
	if !subscription.CanTransitionTo(nextStatus) {
		return tx.Commit()
	}

	if err := s.Repo.UpdateStatus(ctx, tx, subscription.ID, nextStatus); err != nil {
		return err
	}

	return tx.Commit()
}
