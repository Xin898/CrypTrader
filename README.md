# CrypTrader

Self-Contained Crypto Trading System designed as a **Self-Contained System (SCS)**.

The system owns its UI, backend logic, data, integration contracts and deployment lifecycle. It does **not** share a database with TradingAgents or the other trader system.

## Architecture goal

CrypTrader is responsible for deterministic trading decisions and execution. TradingAgents is an upstream **AI market-intelligence service** only.

```text
TradingAgents
     │ AnalysisSignal
     ▼
signal-ingestion
     ▼
strategy-engine
     │ TradeIntent
     ▼
risk-engine
     │ ApprovedTradeIntent
     ▼
order-management
     ▼
execution ─────────► Binance
     │ ExecutionReport
     ▼
portfolio

Binance ─► market-data ─► strategy-engine
```

### Important boundary

```text
AI Signal != Trading Decision
```

TradingAgents may provide direction, confidence, risk factors, data timestamp and validity window. CrypTrader combines those inputs with market state, portfolio state, deterministic strategy rules and risk limits before any order can be submitted.

## Backend modules

| Module | Responsibility |
|---|---|
| `signal-ingestion` | TradingAgents signal/API integration, validation, freshness and idempotency |
| `strategy-engine` | Signal/market-data -> TradeIntent |
| `risk-engine` | Deterministic pre-trade limits, exposure and approval |
| `portfolio` | Positions, cash/balances, PnL and exposure |
| `order-management` | OMS and complete order lifecycle |
| `market-data` | External market feed ingestion and normalization |
| `execution` | Binance order/execution adapter |
| `platform` | Persistence, messaging, observability, configuration, security |

## Naming decisions

- **portfolio** instead of `Depot`: clearer across stock and crypto domains and easier to understand internationally.
- **risk-engine** instead of `RiskController`: `Controller` describes an adapter/API role, while risk is core domain logic.
- **strategy-engine** makes it explicit that this module creates trade intents rather than executing orders.
- **order-management** explicitly represents an OMS boundary.
- **execution** isolates venue-specific APIs from the core trading domain.
- **signal-ingestion** keeps TradingAgents integration outside strategy logic.

## SCS / modular-monolith rule

V1 is intentionally a **modular monolith inside one SCS**, not a collection of fine-grained microservices.

```text
One deployable SCS
├── domain modules with explicit boundaries
├── one owned persistence boundary
├── external integration adapters
└── one operational lifecycle
```

A module should only become an independent service/SCS when there is a concrete reason such as independent ownership, scaling, security, release cadence or cross-platform reuse.

## External market integration

- 24/7 crypto market data
- WebSocket-first market data
- Binance REST/WebSocket integration

## Planned architecture documentation

```text
docs/
├── architecture/
│   ├── system-context.md
│   ├── module-boundaries.md
│   ├── event-flow.md
│   ├── data-ownership.md
│   └── failure-recovery.md
└── adr/
    ├── ADR-001-scs-modular-monolith.md
    ├── ADR-002-tradingagents-contract.md
    ├── ADR-003-order-state-machine.md
    ├── ADR-004-risk-before-execution.md
    └── ADR-005-broker-exchange-adapter.md
```

## Core design principles

1. No shared database between SCSs.
2. External systems are accessed through explicit adapters.
3. Strategy never bypasses Risk.
4. Risk never directly talks to the broker/exchange.
5. Order Management owns order state.
6. Portfolio owns position/exposure state.
7. AI output is versioned, time-bounded decision support only.
8. Every external event must be designed for retry, duplicate and out-of-order handling.
9. Observability and reconciliation are part of the architecture, not afterthoughts.
