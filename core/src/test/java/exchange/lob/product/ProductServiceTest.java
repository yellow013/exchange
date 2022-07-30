package exchange.lob.product;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.LongConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductServiceTest
{

    private ProductService productService;
    private final LongConsumer onAddAsset = a -> {};

    @BeforeEach
    public void setUp()
    {
        productService = ProductService.create();
    }

    private static Map.Entry<String, Asset> assetEntry(Asset asset)
    {
        return entry(asset.getSymbol(), asset);
    }

    private static Map.Entry<String, Product> productEntry(Product product)
    {
        return entry(product.getSymbol(), product);
    }

    @Test
    public void shouldAddAsset()
    {
        productService.addAsset("EUR", (byte)2, onAddAsset);
        final Asset asset = ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        assertThat(productService.assetBySymbol).containsOnly(entry("EUR", asset));
    }

    @Test
    public void shouldNotAddSameSymbolAsset()
    {
        Asset asset = ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        assertThat(productService.assetBySymbol).containsOnly(entry("EUR", asset));
    }

    @Test
    public void shouldAddProduct()
    {
        Asset baseAsset = ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        Asset counterAsset = ProductServiceTestUtils.addAsset(productService, "USD", (byte) 2, onAddAsset);
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(productService, baseAsset.getAssetId(), counterAsset.getAssetId(), makerFee, takerFee);
        final Product product = productService.getProduct(baseAsset.getSymbol() + counterAsset.getSymbol());

        assertEquals(2L, productService.currentAssetId.get());
        assertEquals(1L, productService.currentProductId.get());
        assertThat(productService.assetBySymbol).containsOnly(
            assetEntry(baseAsset),
            assetEntry(counterAsset)
        );
        assertThat(productService.productBySymbol).containsOnly(productEntry(product));
    }

    @Test
    public void shouldAddNonOverLappingProducts()
    {
        Asset baseAsset1 = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, onAddAsset);
        Asset counterAsset1 = ProductServiceTestUtils.addAsset(productService, "USD", (byte) 2, onAddAsset);
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(
            productService,
            baseAsset1.getAssetId(),
            counterAsset1.getAssetId(),
            makerFee,
            takerFee
        );

        final Product product1 = productService.getProduct(baseAsset1.getSymbol() + counterAsset1.getSymbol());

        Asset baseAsset2 = ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        Asset counterAsset2 = ProductServiceTestUtils.addAsset(productService, "PLN", (byte) 2, onAddAsset);
        ProductServiceTestUtils.addProduct(
            productService,
            baseAsset2.getAssetId(),
            counterAsset2.getAssetId(),
            makerFee,
            takerFee
        );

        final Product product2 = productService.getProduct(baseAsset2.getSymbol() + counterAsset2.getSymbol());

        assertEquals(counterAsset2.getAssetId(), productService.currentAssetId.get());
        assertEquals(product2.getProductId(), productService.currentProductId.get());

        assertThat(productService.assetBySymbol).containsOnly(
            assetEntry(baseAsset1),
            assetEntry(counterAsset1),
            assetEntry(baseAsset2),
            assetEntry(counterAsset2)
        );

        assertThat(productService.productBySymbol).containsOnly(
            productEntry(product1),
            productEntry(product2)
        );
    }

    @Test
    public void shouldAddOverlappingProducts()
    {
        Asset baseAsset1 = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, onAddAsset);
        Asset counterAsset1 = ProductServiceTestUtils.addAsset(productService, "USD", (byte) 2, onAddAsset);
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(
            productService,
            baseAsset1.getAssetId(),
            counterAsset1.getAssetId(),
            makerFee,
            takerFee
        );

        final Product product1 = productService.getProduct(baseAsset1.getSymbol() + counterAsset1.getSymbol());

        Asset baseAsset2 = ProductServiceTestUtils.addAsset(productService, "EUR", (byte) 2, onAddAsset);
        ProductServiceTestUtils.addProduct(
            productService,
            baseAsset2.getAssetId(),
            counterAsset1.getAssetId(),
            makerFee,
            takerFee
        );

        final Product product2 = productService.getProduct(baseAsset2.getSymbol() + counterAsset1.getSymbol());

        assertEquals(baseAsset2.getAssetId(), productService.currentAssetId.get());
        assertEquals(product2.getProductId(), productService.currentProductId.get());

        assertThat(productService.assetBySymbol).containsOnly(
            assetEntry(baseAsset1),
            assetEntry(counterAsset1),
            assetEntry(baseAsset2)
        );

        assertThat(productService.productBySymbol).containsOnly(
            productEntry(product1),
            productEntry(product2)
        );
    }

    @Test
    public void shouldNotAddDuplicateProducts()
    {
        Asset baseAsset = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, onAddAsset);
        Asset counterAsset = ProductServiceTestUtils.addAsset(productService, "USD", (byte) 2, onAddAsset);
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(
            productService, baseAsset.getAssetId(), counterAsset.getAssetId(), makerFee, takerFee
        );

        ProductServiceTestUtils.addProduct(
            productService, baseAsset.getAssetId(), counterAsset.getAssetId(), makerFee, takerFee
        );

        final Product product = productService.getProduct(baseAsset.getSymbol() + counterAsset.getSymbol());

        assertThat(productService.assetBySymbol).containsOnly(
            assetEntry(baseAsset),
            assetEntry(counterAsset)
        );

        assertThat(productService.productBySymbol).containsOnly(productEntry(product));
    }

    @Test
    public void shouldEncodeAndDecodeEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);
        ProductService.CODEC.encodeState(productService, exchangeStateEncoder);

        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder()
            .wrap(exchangeStateEncoder.buffer(), exchangeStateEncoder.offset(), 0, 0);
        ProductService decodedProductService = ProductService.CODEC.decodeState(exchangeStateDecoder);
        assertEquals(productService, decodedProductService);
    }

    @Test
    public void shouldEncodeAndDecodeNonEmptySnapshot()
    {
        final Asset baseAsset = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, i -> {});
        final Asset counterAsset = ProductServiceTestUtils.addAsset(productService, "USD", (byte) 2, i -> {});
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAsset.getAssetId(), counterAsset.getAssetId(), makerFee, takerFee);
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);
        ProductService.CODEC.encodeState(productService, exchangeStateEncoder);

        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder()
            .wrap(exchangeStateEncoder.buffer(), exchangeStateEncoder.offset(), 0, 0);
        ProductService decodedProductService = ProductService.CODEC.decodeState(exchangeStateDecoder);
        assertEquals(productService, decodedProductService);
    }
}