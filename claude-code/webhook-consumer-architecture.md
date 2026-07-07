# Webhook Consumer Architecture

Use this guide when building, reviewing, or refactoring webhook consumers in this repository.

Read [README.md](../README.md) first. It defines the architecture and the design goals.

Use the code examples when you need stack-specific patterns:

- [examples/python/django](../examples/python/django)
- [examples/java/spring](../examples/java/spring)
- [examples/go/net-http](../examples/go/net-http)

## What To Optimize For

Build webhook consumers that are:

- secure
- reliable
- idempotent
- consistent
- recoverable
- observable

Also keep the design provider-agnostic and implementation-agnostic.

## Architecture Boundary

Split the flow into three parts:

- provider adapter
- shared webhook pipeline
- domain logic

The provider adapter verifies the request and normalizes provider-specific payloads.

The shared webhook pipeline handles delivery registration, duplicate detection, retry decisions, and processing status.

The domain layer applies the real business rules.

## Preferred Flow

Follow this shape unless the codebase has a strong reason to do otherwise:

1. verify the request
2. validate and normalize the payload
3. register the delivery
4. apply idempotency checks
5. run business logic
6. record the outcome
7. retry or replay when needed

## Coding Rules

- Keep request handlers small.
- Persist or enqueue fast.
- Push slow work into workers or background jobs.
- Use explicit DTOs or typed structs for payload parsing.
- Treat duplicate deliveries as normal.
- Return `2xx` for safe duplicate cases when that prevents noisy retries.
- Protect state transitions when concurrent deliveries can race.
- Log stable identifiers such as provider, delivery ID, and event ID.

## Writing Rules

- Write like an experienced backend developer.
- Keep sentences short and practical.
- Avoid AI-sounding laundry lists.
- Avoid filler and corporate wording.
- Prefer concrete wording.
- Do not over-explain obvious ideas.
