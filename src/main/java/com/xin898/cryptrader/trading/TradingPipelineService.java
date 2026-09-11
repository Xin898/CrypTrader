package com.xin898.cryptrader.trading;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TradingPipelineService {

    private final RiskEngine riskEngine;
    private final ExecutionGateway executionGateway;

    public TradingPipelineService(RiskEngine riskEngine, ExecutionGateway executionGateway) {
        this.riskEngine = riskEngine;
        this.executionGateway = executionGateway;
    }

    public OrderResult handle(AnalysisSignal signal, PortfolioSnapshot portfolio) {
        if (Instant.now().isAfter(signal.validUntil())) {
            throw new IllegalArgumentException("Stale analysis signal: " + signal.analysisId());
        }
        TradeIntent intent = TradeIntent.from(signal);
        return executionGateway.submit(riskEngine.approve(intent, portfolio));
    }

    public record AnalysisSignal(
            String analysisId,
            String symbol,
            Direction direction,
            BigDecimal confidence,
            Instant dataTimestamp,
            Instant validUntil,
            String modelVersion,
            int schemaVersion) {}

    public enum Direction { LONG, SHORT, NEUTRAL }

    public record TradeIntent(String intentId, String symbol, Direction direction, BigDecimal requestedQuantity) {
        static TradeIntent from(AnalysisSignal signal) {
            BigDecimal qty = signal.confidence().multiply(new BigDecimal("0.01")).max(new BigDecimal("0.0001"));
            return new TradeIntent(UUID.randomUUID().toString(), signal.symbol(), signal.direction(), qty);
        }
    }

    public record ApprovedTradeIntent(String intentId, String symbol, Direction direction, BigDecimal approvedQuantity) {}
    public record PortfolioSnapshot(BigDecimal availableBalance, BigDecimal grossExposure) {}
    public record OrderResult(String externalOrderId, String status) {}

    public interface RiskEngine {
        ApprovedTradeIntent approve(TradeIntent intent, PortfolioSnapshot portfolio);
    }

    public interface ExecutionGateway {
        OrderResult submit(ApprovedTradeIntent intent);
        void cancel(String externalOrderId);
        String queryStatus(String externalOrderId);
    }
}
