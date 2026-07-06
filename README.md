# webhook-consumer-handbook
Practical patterns for building production-ready webhook consumers.

## Installable AI Skills

This repository also includes installable AI agent skills for webhook consumer work.

### Codex

```bash
npx skills add Artemooon/webhook-consumer-handbook -a codex
```

### Claude Code

```bash
claude plugin marketplace add Artemooon/webhook-consumer-handbook
claude plugin install webhook-consumer-architecture@webhook-consumer-architecture
```

More details are in [skills/README.md](/Users/artem/coding/webhook-consumer-handbook/skills/README.md).

## Introduction

There is an existing open source guideline [standard-webhooks](https://github.com/standard-webhooks/standard-webhooks), that describes a guideline for the webhook senders.

As a developer, most of the time you work with the webhook consumer, the endpoint that handles incoming webhook event, not the webhook sender.

Not all webhook providers follow standard-webhooks principles or follow them incorrectly, so the core work of making webhook handler safe, reliable and efficient stays on consumer side.

The main goal of this repository is to share common patterns and principles that can be applied in real projects that need webhook consumers.

## Design Goals

These principles guide the handbook:

- **Secure**: Verify incoming requests and never trust webhook payloads blindly.
- **Reliable**: Handle retries, duplicates, partial failures, and temporary downstream outages.
- **Idempotent**: Processing the same event more than once should not corrupt state or trigger duplicate side effects.
- **Consistent**: Prefer correct application state over naive event handling.
- **Recoverable**: Failed events should be visible, retryable, and replayable instead of being silently lost.
- **Observable**: Expose enough logs, metrics, statuses, and errors to debug delivery and processing issues in production.
- **Provider-agnostic**: Use patterns that still work when providers differ in authentication formats, retry behavior, and payload structure.
- **Implementation-agnostic**: Keep the guidance applicable across stacks, languages, and deployment models.


## Verifying Incoming Webhook Requests

Webhooks are just HTTP requests sent by an external system. The consumer should treat every incoming webhook as untrusted until it has been authenticated and validated.

Webhook providers do not follow a single authentication standard. A practical webhook consumer should be ready to support different verification schemes depending on what the provider offers.

Common patterns include:

- **HMAC signature verification**: The provider signs the request body with a shared secret, and the consumer recomputes the signature and compares it against a value sent in a header.
- **SHA-256 header signature verification**: A common variant of HMAC-based verification where the signature is delivered in a request header and computed with SHA-256 or a similar hashing algorithm.
- **Bearer token**: The provider sends a static secret in the `Authorization` header, and the consumer verifies it before processing the event.
- **Basic authentication**: The provider authenticates with an HTTP username and password, which the consumer validates on receipt.
- **Allow/block requests based on the sender IP address**: Some providers publish a list of fixed IP addresses from which webhook requests are sent. The consumer can inspect the source IP address and allow or reject the request based on that list.
- **Custom secret in the callback URL**: If the provider does not support request authentication, the consumer can embed an unguessable secret in the webhook URL, such as a path segment or query parameter, and verify it on receipt.

The consumer should implement at least one authentication method. Any secrets, signing keys, or credentials provided by the webhook provider should be treated as sensitive and stored securely.

## SSL/TLS Certificates

The webhook consumer should expose its endpoint over HTTPS so that data in transit is encrypted between the provider and the consumer.

TLS helps protect webhook payloads, headers, and credentials from interception or tampering in transit. This is especially important when using methods such as Basic authentication or bearer tokens, because those credentials can be exposed to man-in-the-middle attacks if the request is sent over plain HTTP.

## Validating Webhook Payloads

Payload validation confirms that the request body has the structure and required fields that the consumer expects.

Webhook consumers should parse incoming payloads into explicit validator classes or DTO objects before business logic runs. This helps ensure that the application works with a known and predictable data shape instead of passing around raw JSON dictionaries or loosely typed objects.

This approach is useful for:

- **Schema validation**: Reject requests with missing required fields, unexpected types, invalid enum values, or malformed nested objects.
- **Type normalization**: Convert incoming values into the expected internal representation, such as timestamps, decimals, booleans, and nested entities.
- **Clear boundaries**: Keep transport-layer payload parsing separate from business logic, which makes the webhook flow easier to reason about and test.
- **Safer evolution**: Make provider payload changes easier to detect when fields are added, removed, or changed unexpectedly.

The exact implementation depends on the stack, but the pattern is broadly the same. In Python, libraries such as Pydantic are commonly used for structured validation. In Java and Spring applications, validation is often handled with DTO classes and Bean Validation annotations. In Node.js and Express applications, teams often use schema validators such as Zod, Joi, or class-validator.

If payload validation fails, the consumer should usually return a client error such as `400 Bad Request` or, when the stack distinguishes semantic validation separately, `422 Unprocessable Entity`. `429 Too Many Requests` is not appropriate for an invalid payload because it signals rate limiting, not schema or data validation failure.

The exact retry behavior depends on the webhook provider. Some providers retry on any non-`2xx` response, while others only retry on specific status codes. Even so, malformed payloads are usually permanent failures rather than temporary ones, so `4xx` responses are a better fit than `5xx` responses when the request body does not match the expected contract.

Temporary internal problems, such as database outages or downstream service failures, should still return `5xx` responses so the provider can retry later.

## Reliable Processing

Reliable webhook handling starts with keeping the request path short and predictable. The consumer should avoid doing too much work before responding to the sender.

A practical pattern is:

- **Authenticate and validate early**: Reject invalid or unauthenticated requests before they enter the processing flow.
- **Persist or enqueue quickly**: Store the event or hand it off to a background job as soon as possible.
- **Return a fast response**: Send a `2xx` response once the event has been accepted for processing, instead of waiting for all downstream work to finish.
- **Process heavy work asynchronously**: Database updates, external API calls, emails, and other slow operations are usually better handled in a worker or background job.

If the consumer keeps the HTTP request open while it performs slow business logic or waits on other APIs, timeouts become more likely and the chance of failure increases.

Retries should also exist inside the consumer, not only on the provider side. If webhook processing depends on external APIs, queues, databases, or other downstream systems, the consumer should retry temporary failures with bounded retry rules such as a limited number of attempts and increasing delay between attempts.

Not every failure should be retried. Validation errors, missing required fields, and other permanent problems should fail immediately. Transient problems such as network timeouts, temporary API errors, or short-lived database issues are better candidates for retries.

Partial failures should be handled explicitly. If one step succeeds but a later step fails, the consumer should record the state clearly and retry only the unfinished work instead of restarting the entire flow blindly.

## Idempotent Webhook Consumer Execution

Webhook providers may deliver the same event more than once. This can happen because of retries, network issues or provider-side wrong delivery. A webhook consumer should treat repeated delivery as a normal case.

The consumer should use an idempotency key to detect whether the same event has already been accepted or processed. In many webhook integrations, the provider includes an event ID that can be used for this purpose.

A practical pattern is:

- **Build a key from the event identity**: Use the provider event ID from the webhook payload.
- **Store processed keys**: Keep the key in cache storage or database table.
- **Discard duplicates safely**: If the same event arrives again and the key already exists, skip the business logic.
- **Return a successful status for duplicates**: Respond with `200 OK` for already handled events so the provider does not keep retrying delivery because of a non-`2xx` status.

This protects the system from duplicate side effects such as repeated inserts, duplicate emails or repeated billing actions.

Cache-based deduplication can work well when the key lifetime matches the provider retry window.

Idempotency should be checked as early as possible in the processing flow, before the consumer performs side effects or starts expensive downstream work.

## Consistent Application State

Webhook providers do not always deliver events in order or one at a time. Protect your application state from concurrent requests and inconsistent provider behavior.

- **Add guards before state changes**: Check the current record state before applying an update so an older or unexpected event does not overwrite a newer valid state.
- **Use locking when updates must be serialized**: Database row locks, mutexes, or other concurrency controls can prevent two webhook requests from modifying the same resource at the same time.
- **Make transitions explicit**: Allow only valid state transitions instead of trusting every incoming event to be correct.
- **Prefer reconciliation to blind writes**: If the payload looks incomplete, late, or contradictory, fetch the latest provider state or mark the event instead of forcing an unsafe update.

This matters most when two webhook deliveries can race, such as payment updates, subscription changes, or inventory events.

## Recoverable Processing

Webhook processing should fail in a controlled way. If an event cannot be processed because of a temporary error, rate limit, downstream outage, or internal failure, it should stay visible and be retried instead of being silently dropped.

- **Track failed events explicitly**: Store failed webhook events with retry metadata and processing status so they can be inspected and replayed later.
- **Retry in the background**: Use background jobs or workers to retry temporary failures after a delay instead of blocking the original request path.
- **Mark retry progress clearly**: Update the event state so recovery logic stays predictable.
- **Use durable storage for critical events**: Redis or Memcached can help with short-lived retry coordination, but important failed events are safer in a database or queue that survives restarts and eviction.

This lets you recover from temporary failures without losing the event or rebuilding the flow by hand.

## Observable Processing

Webhook consumers should expose enough information to show what arrived, what ran, what failed, and what needs attention. Without that visibility, production issues are hard to debug.

- **Log each delivery and outcome**: Record the provider, event ID, delivery time, processing result, and key error details for each webhook event.
- **Track statuses and retry counts**: Keep event states such as accepted, processing, failed, retried, and completed so operators can see where events are stuck.
- **Use centralized log or event streams**: Send webhook logs to systems such as CloudWatch, ELK, Datadog, or similar tooling so teams can search, filter, and investigate production issues.

This gives the team enough context to diagnose provider issues, investigate inconsistencies, and step in when a webhook flow goes wrong.

## Provider-Agnostic Consumer Architecture

Webhook providers rarely agree on authentication, headers, event names, retry behavior, or payload shape. A maintainable consumer should keep those differences at the edge and keep the rest of the system working with one internal event model.

- **Provider adapter**: Verifies the request, reads provider-specific headers, validates the payload, and maps external event names and fields into one internal event format.
- **Shared webhook pipeline**: Registers deliveries, applies idempotency checks, decides whether failures should be retried, and records processing status.
- **Domain layer**: Runs the actual business logic, such as saving records, calculating values, changing permissions, sending emails, or scheduling follow-up work.

The provider adapter should be the only place that knows the provider-specific request format. Its job is to turn the incoming webhook into the same internal event shape every time.

That internal event should stay simple and predictable. In most systems it includes the provider name, delivery ID, event ID, internal event type, affected resource ID, normalized payload, and basic tracing metadata.

After that, the rest of the system should process the event the same way no matter which provider sent it. This keeps provider parsing separate from webhook handling, and webhook handling separate from business logic.

This structure is easier to extend and maintain. Adding a new provider usually means adding a new adapter, not rewriting the whole flow. If a provider changes its headers, payload fields, or event names, the change usually stays there.

## Implementation-Agnostic Guidance

The important part is the responsibility of each step, not the specific implementation.

Describe the system by what it does:

- **Verify the request**
- **Validate and normalize the payload**
- **Register the delivery**
- **Check idempotency**
- **Apply business rules**
- **Record the outcome**
- **Retry or replay when needed**

Different teams will wire that flow differently. The guidance stays useful as long as those responsibilities are handled clearly and safely.

That makes the handbook easier to reuse across stacks.
