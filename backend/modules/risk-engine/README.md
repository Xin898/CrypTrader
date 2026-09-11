# risk-engine

Deterministic pre-trade risk gate for limits, exposure, position constraints and approved quantity. Produces ApprovedTradeIntent or rejection reasons.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
