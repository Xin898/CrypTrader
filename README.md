# CrypTrader

Self-Contained Crypto Trading System designed as a **Self-Contained System (SCS)**.

CrypTrader owns its UI, backend logic, data, integration contracts and deployment lifecycle. It does **not** share a database with TradingAgents or StockTrader.

## Architecture goal

TradingAgents is an upstream **AI market-intelligence service**. CrypTrader remains responsible for deterministic strategy, risk, order management and execution.

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
| `portfolio` | Positions, balances, PnL and exposure |
| `order-management` | OMS and complete order lifecycle |
| `market-data` | Binance WebSocket/REST feed ingestion and normalization |
| `execution` | Binance order/execution adapter |
| `platform` | Persistence, messaging, observability, configuration, security |

## Naming decisions

- **portfolio** instead of `Depot`: clearer across asset classes and internationally understandable.
- **risk-engine** instead of `RiskController`: risk is core domain logic, not an HTTP controller.
- **strategy-engine** creates trade intents but does not execute orders.
- **order-management** represents the OMS boundary.
- **execution** isolates Binance-specific APIs from core trading logic.
- **signal-ingestion** keeps TradingAgents integration outside strategy logic.

## SCS / modular-monolith rule

V1 is intentionally a **modular monolith inside one SCS**, not a collection of fine-grained microservices.

A module should only become an independent service/SCS when there is a concrete reason such as independent ownership, scaling, security, release cadence or cross-platform reuse.

## Crypto-specific integration

- 24/7 trading model
- WebSocket-first market data
- Binance REST/WebSocket integration
- reconnect / resubscribe handling
- sequence and order-book consistency
- symbol precision / lot size / tick size
- exchange rate limits and reconciliation

## Core design principles

1. No shared database between SCSs.
2. External systems are accessed through explicit adapters.
3. Strategy never bypasses Risk.
4. Risk never directly talks to Binance.
5. Order Management owns order state.
6. Portfolio owns position/exposure state.
7. AI output is versioned, time-bounded decision support only.
8. Every external event must be designed for retry, duplicate and out-of-order handling.
9. Observability and reconciliation are part of the architecture.
