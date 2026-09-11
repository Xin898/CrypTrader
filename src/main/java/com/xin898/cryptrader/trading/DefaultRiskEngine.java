package com.xin898.cryptrader.trading;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.xin898.cryptrader.trading.TradingPipelineService.ApprovedTradeIntent;
import com.xin898.cryptrader.trading.TradingPipelineService.PortfolioSnapshot;
import com.xin898.cryptrader.trading.TradingPipelineService.RiskEngine;
import com.xin898.cryptrader.trading.TradingPipelineService.TradeIntent;

@Component
public class DefaultRiskEngine implements RiskEngine {

    private static final BigDecimal MAX_EXPOSURE = new BigDecimal("50000");

    @Override
    public ApprovedTradeIntent approve(TradeIntent intent, PortfolioSnapshot portfolio) {
        if (portfolio.grossExposure().compareTo(MAX_EXPOSURE) >= 0) {
            throw new IllegalStateException("Crypto exposure limit exceeded");
        }

        BigDecimal approved = intent.requestedQuantity().min(new BigDecimal("1.0"));
        return new ApprovedTradeIntent(intent.intentId(), intent.symbol(), intent.direction(), approved);
    }
}
