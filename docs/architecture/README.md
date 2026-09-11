# CrypTrader Architecture

## Architectural style

CrypTrader is an independently deployable **Self-Contained System (SCS)**. V1 uses a **modular monolith** internally, with explicit domain-module boundaries and Ports & Adapters for external infrastructure.

## NFRs and architecture principles

| ID | Requirement | Architecture consequence |
|---|---|---|
| NFR-01 | Stateless Trader instances | No business/session state is owned by a specific application instance. Persistent state lives in owned external stores so instances can be replaced freely. |
| NFR-02 | Horizontal scalability | Multiple CrypTrader instances can run behind a load balancer / Kubernetes Service and scale independently according to workload. |
| NFR-03 | Database per SCS | CrypTrader owns **crypto-trader-db**. No other SCS may read/write it directly. |
| NFR-04 | No shared database | Cross-SCS data exchange uses explicit APIs or versioned events, preserving service autonomy. |
| NFR-05 | Loose coupling to trading venue | Core trading logic depends on application/domain ports, never directly on Binance SDK/API types. |
| NFR-06 | Independent deployment | CrypTrader can be built, released, deployed and scaled independently of TradingAgents, the other Trader and TradeMonitor. |
| NFR-07 | Explicit/versioned contracts | APIs and events must be versioned and designed for backward compatibility, idempotency, duplicates and out-of-order delivery. |
| NFR-08 | Resilience & recovery | External calls require bounded timeouts/retry policy; state must be recoverable/reconcilable after process or venue failures. |
| NFR-09 | Observability | Requests/events/orders carry correlation identifiers and expose logs, metrics and traces; business events are published to TradeMonitor. |

### Stateless does not mean stateless business

Trading is stateful by nature. The requirement is that **state is not bound to a process instance**.

```text
                    Load Balancer / K8s Service
                              │
               ┌──────────────┼──────────────┐
               ▼              ▼              ▼
          Trader Pod 1   Trader Pod 2   Trader Pod N
               │              │              │
               └──────────────┼──────────────┘
                              ▼
                    CrypTrader-owned stores
                  DB / Redis / Event infrastructure
```

A pod can fail and another instance can continue from durable/shared infrastructure state.

> Concurrency-sensitive workflows still require explicit ownership/coordination. Stateless HTTP/application instances alone do not solve duplicate order processing, ordering or concurrent updates.

## Data ownership

```text
TradingAgents DB       CrypTrader DB       TradeMonitor DB
      ▲                     ▲                  ▲
      │ owned only          │ owned only       │ owned only
      └─────────────────────┴──────────────────┘

Cross-boundary access: API / versioned events only
```

CrypTrader owns order, portfolio and trading state. Direct database access from TradingAgents, TradeMonitor or another Trader is forbidden.

## Ports & Adapters

The trading domain is isolated from the concrete venue.

```text
strategy-engine
      ↓
risk-engine
      ↓
order-management
      ↓
ExecutionPort
      ↓
Binance Adapter
      ↓
Binance
```

Example port:

```java
public interface ExecutionGateway {
    OrderResult submit(OrderRequest request);
    void cancel(String orderId);
    OrderStatus query(String orderId);
}
```

The core modules must not import Binance SDK/domain classes. Mapping happens inside the adapter.

The same rule applies to market data:

```text
Binance
   ↓
MarketDataAdapter
   ↓
MarketDataPort
   ↓
normalized internal market events
   ↓
strategy-engine
```

## Module flow

```text
TradingAgents AnalysisSignal
          ↓
   signal-ingestion
          ↓
    strategy-engine
          ↓
      risk-engine
          ↓
   order-management
          ↓
    ExecutionPort
          ↓
       execution
          ↓
   Binance
          ↓
   ExecutionReport
          ↓
      portfolio
          ↓
 versioned business events
          ↓
     TradeMonitor
```

## Horizontal-scaling correctness

Horizontal scaling introduces distributed concurrency. The implementation must therefore explicitly design for:

- idempotency keys for commands/events
- duplicate event delivery
- optimistic locking or equivalent protection for concurrent state changes
- deterministic order ownership/state transitions
- safe retry semantics around external order submission
- partitioning/ordering strategy where Kafka is used
- recovery and reconciliation after uncertain external outcomes

The target is **scale-out without duplicate trades or corrupted portfolio state**, not merely the ability to start more pods.

## Dependency rules

- Strategy never bypasses Risk.
- Risk never directly calls Binance.
- Order Management owns order state.
- Portfolio owns position/exposure state.
- Venue SDK/API dependencies live behind adapters.
- No module reaches directly into another module's persistence implementation.
- No SCS reaches directly into another SCS's database.

## Future split criteria

An internal module becomes an independent service only when justified by a concrete requirement such as:

- separate team ownership
- independent scaling profile
- independent release cadence
- security/isolation requirement
- cross-platform reuse
- measured operational bottleneck

Microservice boundaries are driven by autonomy and NFRs, not by class/package boundaries.
