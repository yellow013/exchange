package exchange.lob;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import exchange.lob.events.admin.AdminRequestProcessor;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.events.trading.TradingRequestProcessor;
import exchange.lob.match.MatchingEngine;
import exchange.lob.node.Stateful;
import exchange.lob.product.ProductService;
import exchange.lob.risk.RiskEngine;
import exchange.lob.user.UserService;

import java.util.Objects;

public final class Exchange
{
    private final UserService userService;
    private final RiskEngine riskEngine;
    private final MatchingEngine matchingEngine;
    private final ProductService productService;

    private AdminRequestProcessor adminRequestProcessor;
    private TradingRequestProcessor tradingRequestProcessor;

    public static final Codec CODEC = new Codec();

    Exchange(
        UserService userService,
        RiskEngine riskEngine,
        MatchingEngine matchingEngine,
        ProductService productService,
        AdminRequestProcessor adminRequestProcessor,
        TradingRequestProcessor tradingRequestProcessor
    )
    {
        this.userService = userService;
        this.riskEngine = riskEngine;
        this.matchingEngine = matchingEngine;
        this.productService = productService;
        this.adminRequestProcessor = adminRequestProcessor;
        this.tradingRequestProcessor = tradingRequestProcessor;
    }

    public static Exchange bootstrap(
        final AdminRequestProcessor adminRequestProcessor,
        final TradingRequestProcessor tradingRequestProcessor,
        final OrderBookEvents orderBookEvents
    )
    {
        return Exchange.builder()
            .adminRequestProcessor(adminRequestProcessor)
            .tradingRequestProcessor(tradingRequestProcessor)
            .userService(UserService.create())
            .productService(ProductService.create())
            .riskEngine(RiskEngine.withOrderBookEvents(orderBookEvents))
            .matchingEngine(MatchingEngine.withOrderBookEvents(orderBookEvents))
            .build();
    }

    public static ExchangeBuilder builder()
    {
        return new ExchangeBuilder();
    }

    public void bindState()
    {
        adminRequestProcessor.bindUserService(userService)
            .bindRiskEngine(riskEngine)
            .bindProductService(productService)
            .bindMatchingEngine(matchingEngine);

        tradingRequestProcessor.bindUserService(userService)
            .bindProductService(productService)
            .bindRiskEngine(riskEngine)
            .bindMatchingEngine(matchingEngine);
    }

    public void bindAdminRequestProcessor(final AdminRequestProcessor adminRequestProcessor)
    {
        this.adminRequestProcessor = adminRequestProcessor;
    }

    public void bindTradingRequestProcessor(final TradingRequestProcessor tradingRequestProcessor)
    {
        this.tradingRequestProcessor = tradingRequestProcessor;
    }

    public UserService getUserService()
    {
        return this.userService;
    }

    public RiskEngine getRiskEngine()
    {
        return this.riskEngine;
    }

    public MatchingEngine getMatchingEngine()
    {
        return this.matchingEngine;
    }

    public ProductService getProductService()
    {
        return this.productService;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Exchange exchange = (Exchange)o;
        return Objects.equals(userService, exchange.userService) && Objects.equals(riskEngine, exchange.riskEngine) && Objects.equals(
            matchingEngine,
            exchange.matchingEngine
        ) && Objects.equals(productService, exchange.productService) && Objects.equals(
            adminRequestProcessor,
            exchange.adminRequestProcessor
        ) && Objects.equals(tradingRequestProcessor, exchange.tradingRequestProcessor);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userService, riskEngine, matchingEngine, productService, adminRequestProcessor, tradingRequestProcessor);
    }

    public static class Codec implements Stateful<Exchange>
    {

        @Override
        public Exchange decodeState(final ExchangeStateDecoder exchangeStateDecoder)
        {
            return Exchange.builder()
                .userService(UserService.CODEC.decodeState(exchangeStateDecoder))
                .productService(ProductService.CODEC.decodeState(exchangeStateDecoder))
                .riskEngine(RiskEngine.CODEC.decodeState(exchangeStateDecoder))
                .matchingEngine(MatchingEngine.CODEC.decodeState(exchangeStateDecoder))
                .build();
        }

        @Override
        public void encodeState(final Exchange exchange, final ExchangeStateEncoder exchangeStateEncoder)
        {
            UserService.CODEC.encodeState(exchange.getUserService(), exchangeStateEncoder);
            ProductService.CODEC.encodeState(exchange.getProductService(), exchangeStateEncoder);
            RiskEngine.CODEC.encodeState(exchange.getRiskEngine(), exchangeStateEncoder);
            MatchingEngine.CODEC.encodeState(exchange.getMatchingEngine(), exchangeStateEncoder);
        }
    }

    public static class ExchangeBuilder
    {
        private UserService userService;
        private RiskEngine riskEngine;
        private MatchingEngine matchingEngine;
        private ProductService productService;
        private AdminRequestProcessor adminRequestProcessor;
        private TradingRequestProcessor tradingRequestProcessor;

        ExchangeBuilder()
        {
        }

        public ExchangeBuilder userService(UserService userService)
        {
            this.userService = userService;
            return this;
        }

        public ExchangeBuilder riskEngine(RiskEngine riskEngine)
        {
            this.riskEngine = riskEngine;
            return this;
        }

        public ExchangeBuilder matchingEngine(MatchingEngine matchingEngine)
        {
            this.matchingEngine = matchingEngine;
            return this;
        }

        public ExchangeBuilder productService(ProductService productService)
        {
            this.productService = productService;
            return this;
        }

        public ExchangeBuilder adminRequestProcessor(AdminRequestProcessor adminRequestProcessor)
        {
            this.adminRequestProcessor = adminRequestProcessor;
            return this;
        }

        public ExchangeBuilder tradingRequestProcessor(TradingRequestProcessor tradingRequestProcessor)
        {
            this.tradingRequestProcessor = tradingRequestProcessor;
            return this;
        }

        public Exchange build()
        {
            return new Exchange(userService, riskEngine, matchingEngine, productService, adminRequestProcessor, tradingRequestProcessor);
        }

        @Override
        public String toString()
        {
            return "ExchangeBuilder{" +
                "userService=" + userService +
                ", riskEngine=" + riskEngine +
                ", matchingEngine=" + matchingEngine +
                ", productService=" + productService +
                ", adminRequestProcessor=" + adminRequestProcessor +
                ", tradingRequestProcessor=" + tradingRequestProcessor +
                '}';
        }
    }
}
