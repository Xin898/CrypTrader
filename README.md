# CrypTrader

CrypTrader is the **crypto-trading Self-Contained System (SCS)** in the trading platform. It consumes market-intelligence signals from TradingAgents, combines them with deterministic strategy and risk rules, manages orders and positions, and integrates with **Binance** for market data and execution.

## Responsibilities

- consume and validate TradingAgents `AnalysisSignal`
- process 24/7 crypto market data
- maintain WebSocket connectivity and recover market-data streams
- create deterministic `TradeIntent` decisions
- enforce pre-trade risk checks
- manage order lifecycle and exchange reconciliation
- maintain balances, positions, PnL and exposure
- publish trading/business events for TradeMonitor
- isolate Binance-specific APIs behind adapters

## Overall architecture

```text
                    TradingAgents
                         │
                    AnalysisSignal
                         ▼
                  signal-ingestion
                         ▼
Binance ───────► market-data ─► strategy-engine
                         │
                    TradeIntent
                         ▼
                     risk-engine
                         │
                ApprovedTradeIntent
                         ▼
                  order-management
                         ▼
                      execution
                         │
                         ▼
                       Binance
                         │
                  ExecutionReport
                         ▼
                     portfolio
                         │
              trading/business events
                         ▼
                    TradeMonitor
```

**AI Signal != Trading Decision.** TradingAgents provides decision-support information only. CrypTrader owns deterministic strategy, risk and execution.

## Modules

| Module | Responsibility |
|---|---|
| `signal-ingestion` | TradingAgents contract, freshness, versioning and idempotency |
| `market-data` | Binance REST/WebSocket feed, normalization and recovery |
| `strategy-engine` | Market/AI inputs → `TradeIntent` |
| `risk-engine` | Position/exposure/limit checks and trade approval |
| `order-management` | OMS and order state machine |
| `execution` | Binance execution adapter |
| `portfolio` | Balances, positions, PnL and exposure |
| `platform` | Persistence, messaging, security, configuration and observability |

## Crypto-specific concerns

- 24/7 trading
- WebSocket reconnect/resubscribe
- sequence and order-book consistency
- symbol precision, lot size and tick size
- exchange rate limits
- duplicate/out-of-order events
- execution and balance reconciliation

## SCS boundary

V1 is a **modular monolith inside one independently deployable SCS**.

- no shared database with TradingAgents, StockTrader or TradeMonitor
- Strategy cannot bypass Risk
- Risk cannot call Binance directly
- Order Management owns order state
- Portfolio owns balances/position/exposure state
- Binance SDK/API dependencies stay behind market-data/execution adapters

## Platform context

```text
TradingAgents ──AnalysisSignal──► CrypTrader
                                     │
                                     ├──► Binance
                                     │
                                     └──trading events──► TradeMonitor
```

## Architecture documentation

```text
docs/
├── architecture/
└── adr/
```

Planned ADR topics include SCS boundaries, TradingAgents contracts, order state/idempotency, mandatory risk gates, Binance adapters, WebSocket recovery, event delivery and reconciliation.
