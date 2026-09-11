# strategy-engine

Transforms normalized market data plus AI/quant inputs into TradeIntent. It never submits orders directly.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
