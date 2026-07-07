package examples

import (
	"errors"
	"net/http"
)

type WebhookAuthenticator interface {
	Authenticate(r *http.Request) error
}

type BasicAuthWebhookAuthenticator struct {
	ExpectedUsername string
	ExpectedPassword string
}

func (a BasicAuthWebhookAuthenticator) Authenticate(r *http.Request) error {
	username, password, ok := r.BasicAuth()
	if !ok {
		return errors.New("missing basic auth credentials")
	}

	if username != a.ExpectedUsername || password != a.ExpectedPassword {
		return errors.New("invalid basic auth credentials")
	}

	return nil
}
