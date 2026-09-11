package com.xin898.cryptrader.adapter.binance;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.xin898.cryptrader.trading.TradingPipelineService.ApprovedTradeIntent;
import com.xin898.cryptrader.trading.TradingPipelineService.ExecutionGateway;
import com.xin898.cryptrader.trading.TradingPipelineService.OrderResult;

@Component
@Primary
public class BinanceExecutionAdapter implements ExecutionGateway {

    @Override
    public OrderResult submit(ApprovedTradeIntent intent) {
        // TODO epic: connect to Binance test environment.
        // Symbol precision, lot size, rate limits and exchange errors stay in this adapter.
        return new OrderResult("binance-test-" + UUID.randomUUID(), "ACCEPTED");
    }

    @Override
    public void cancel(String externalOrderId) {
        // TODO integrate Binance cancel endpoint.
    }

    @Override
    public String queryStatus(String externalOrderId) {
        // TODO integrate Binance query/reconciliation endpoint.
        return "UNKNOWN";
    }
}
