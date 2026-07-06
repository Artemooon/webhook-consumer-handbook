# Skills

This folder contains installable AI agent skills for webhook consumer work.

## Available Skills

### Codex

- `webhook-consumer-architecture`

### Claude Code

- `../claude-code/webhook-consumer-architecture.md`

## Install For Codex

```bash
npx skills add Artemooon/webhook-consumer-handbook -a codex
```

## Install For Claude Code

```bash
claude plugin marketplace add Artemooon/webhook-consumer-handbook
claude plugin install webhook-consumer-architecture@webhook-consumer-architecture
```

## Notes

- Codex installs the skill through `skills-lock.json`.
- Claude Code installs the package through `.claude-plugin/`.
- You do not need to install this repository as an application to use the skills.

## What The Skill Covers

The skill is based on this repository's webhook guidance:

- secure request verification
- payload validation and normalization
- reliable processing
- idempotency
- consistency under concurrent deliveries
- recoverability and replay
- observability
- provider-agnostic webhook architecture

It also points to the language examples in `examples/python/django`, `examples/java/spring`, and `examples/go/net-http`.
