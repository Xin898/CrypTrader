# execution

Owns Binance order submission/cancel/query integration and translates exchange execution reports into internal domain events.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
