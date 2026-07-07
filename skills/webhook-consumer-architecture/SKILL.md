---
name: webhook-consumer-architecture
description: Design, review, and implement production-ready webhook consumers using the patterns in this repository. Use when Codex needs to build or improve webhook endpoints, background processing, idempotency, retries, payload validation, observability, or provider adapters, especially when the work should follow this repo's README guidance and example code.
---

# Webhook Consumer Architecture

Use this skill when working on webhook consumers that should follow the patterns documented in this repository.

Start with [README.md](./README.md). Use it as the source of truth for the design goals and the processing model.

Use the examples folder when the user wants concrete code patterns:

- [examples/python/django](./examples/python/django/README.md)
- [examples/java/spring](./examples/java/spring/README.md)
- [examples/go/net-http](./examples/go/net-http/README.md)

## Core Model

Treat webhook handling as three separate concerns:

- provider adapter
- shared webhook pipeline
- domain logic

Keep provider-specific request handling at the edge. Normalize the webhook into one internal event shape. Run business logic after normalization.

## Design Goals

Apply the design goals from the handbook:

- secure
- reliable
- idempotent
- consistent
- recoverable
- observable
- provider-agnostic
- implementation-agnostic

Do not treat these as slogans. Map them to concrete code paths, storage choices, and failure behavior.

## Working Rules

- Verify the request before trusting headers or payloads.
- Parse raw payloads into explicit DTOs, validator objects, or typed structs before business logic.
- Keep the HTTP request path short. Persist or enqueue quickly. Push heavy work to background processing.
- Handle duplicate deliveries explicitly. Return `2xx` for already handled duplicates when that prevents useless retries.
- Protect state transitions when multiple deliveries can touch the same resource.
- Record retryable failures and make replay possible.
- Log stable identifiers such as provider, delivery ID, and event ID.
- Keep provider-specific event names and field mapping out of the domain layer.

## Implementation Pattern

When building or refactoring a webhook flow, prefer this order:

1. verify the request
2. validate and normalize the payload
3. register the delivery
4. apply idempotency checks
5. run business logic
6. record the outcome
7. retry or replay when needed

If the real system combines some of these steps atomically, keep the responsibilities clear anyway.

## Output Style

Write like an experienced backend developer explaining the topic to another developer.

- Keep sentences short and practical.
- Avoid AI-sounding laundry lists.
- Avoid filler and corporate phrasing.
- Prefer concrete wording over generic abstractions.
- Do not over-explain obvious ideas.
- When possible, say the same thing in fewer words.

## Example Routing

Use the Django examples for reusable authentication classes, DTO normalization, and request-to-worker flows.

Use the Spring examples for interface-driven adapters, transport DTOs, and transaction boundaries.

Use the Go examples for explicit handler pipelines, storage-backed idempotency, and standard `net/http` request handling.
