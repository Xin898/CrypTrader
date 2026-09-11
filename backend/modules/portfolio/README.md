# portfolio

Owns crypto positions, balances, realized/unrealized PnL and exposure. This is the clearer domain replacement for Depot.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
