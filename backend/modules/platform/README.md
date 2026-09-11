# platform

Cross-cutting infrastructure only: persistence adapters, messaging, configuration, telemetry, security and operational concerns. No trading business rules.

## Boundary rule

This module exposes explicit application/domain interfaces. Other modules should not reach into its persistence or exchange-adapter implementation directly.
