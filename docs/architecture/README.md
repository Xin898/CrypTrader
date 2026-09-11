# Architecture

## SCS boundary

CrypTrader is one independently deployable Self-Contained System. Internal modules are architectural boundaries, not independently deployed microservices in V1.

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
       execution
          ↓
   Binance
          ↓
   ExecutionReport
          ↓
      portfolio
```

Market data enters through `market-data` and is normalized before being consumed by the strategy module.

## Dependency direction

Domain modules must not depend on venue-specific SDKs. Venue SDK/API code belongs behind `market-data` and `execution` adapters.

## Future split criteria

A module becomes an independent service only if one or more are true:
- separate team ownership
- independent scaling profile
- independent release cadence
- security/isolation requirement
- multiple SCSs need the capability
- measured operational bottleneck
