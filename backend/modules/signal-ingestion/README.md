# signal-ingestion

Consumes versioned TradingAgents analysis signals. Validates schema version, freshness, validity window and idempotency before mapping them into internal commands.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
