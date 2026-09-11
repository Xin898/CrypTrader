# market-data

Owns Binance WebSocket/REST market-data connectivity, reconnect/resubscribe, normalization and sequence/order-book consistency.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
