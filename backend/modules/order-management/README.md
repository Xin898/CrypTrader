# order-management

Owns the complete order lifecycle: create, validate, submit, cancel, amend, reconcile, fill/partial-fill/reject and idempotency.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
