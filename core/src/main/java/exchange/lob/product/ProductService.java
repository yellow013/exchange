package exchange.lob.product;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.node.Stateful;
import exchange.lob.node.client.response.AddAssetResponse;
import exchange.lob.node.client.response.AddProductResponse;
import exchange.lob.node.client.response.GetAssetsResponse;
import exchange.lob.node.client.response.GetProductsResponse;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.MutableLong;
import org.agrona.collections.Object2ObjectHashMap;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.function.LongConsumer;


public class ProductService
{

    final MutableLong currentAssetId;
    final MutableLong currentProductId;

    final Object2ObjectHashMap<String, Asset> assetBySymbol;
    final Long2ObjectHashMap<Asset> assetByAssetId;
    final Object2ObjectHashMap<String, Product> productBySymbol;

    public static final Codec CODEC = new Codec();

    public static ProductService create()
    {
        return new ProductService(
            new MutableLong(),
            new MutableLong(),
            new Object2ObjectHashMap<>(),
            new Long2ObjectHashMap<>(),
            new Object2ObjectHashMap<>()
        );
    }

    public ProductService(
        final MutableLong currentAssetId,
        final MutableLong currentProductId,
        final Object2ObjectHashMap<String, Asset> assetBySymbol,
        final Long2ObjectHashMap<Asset> assetByAssetId,
        final Object2ObjectHashMap<String, Product> productBySymbol
    )
    {
        this.currentAssetId = currentAssetId;
        this.currentProductId = currentProductId;
        this.assetBySymbol = assetBySymbol;
        this.assetByAssetId = assetByAssetId;
        this.productBySymbol = productBySymbol;
    }

    public AddAssetResponse addAsset(final String symbol, final byte scale, final LongConsumer addAssetCallback)
    {
        return assetBySymbol.values().stream()
            .filter(a -> a.getSymbol().equals(symbol))
            .findFirst()
            .map(a -> new AddAssetResponse(ExchangeResponseCode.INVALID_ASSET, Long.MIN_VALUE))
            .orElseGet(() -> {
                final long assetId = currentAssetId.incrementAndGet();

                final Asset asset = Asset.builder()
                    .assetId(assetId)
                    .symbol(symbol)
                    .scale(scale)
                    .build();

                assetBySymbol.put(symbol, asset);
                assetByAssetId.put(assetId, asset);

                addAssetCallback.accept(assetId);

                return new AddAssetResponse(ExchangeResponseCode.SUCCESS, assetId);
            });
    }

    public AddProductResponse addProduct(final long baseAssetId, final long counterAssetId, final long makerFee, final long takerFee)
    {
        final Asset baseAsset = assetByAssetId.get(baseAssetId);
        final Asset counterAsset = assetByAssetId.get(counterAssetId);

        if (baseAssetId == counterAssetId || (baseAsset == null || counterAsset == null))
        {
            return new AddProductResponse(ExchangeResponseCode.INVALID_PRODUCT, Long.MIN_VALUE);
        }

        final boolean productExists = productBySymbol.values().stream()
            .anyMatch(p -> p.getBaseAsset().equals(baseAsset) && p.getCounterAsset().equals(counterAsset));

        if (productExists)
        {
            return new AddProductResponse(ExchangeResponseCode.DUPLICATE_PRODUCT, Long.MIN_VALUE);
        }

        long productId = currentProductId.incrementAndGet();
        final Product product = Product.builder()
            .productId(productId)
            .baseAsset(baseAsset)
            .counterAsset(counterAsset)
            .makerFee(makerFee)
            .takerFee(takerFee)
            .build();

        productBySymbol.put(product.getSymbol(), product);
        return new AddProductResponse(ExchangeResponseCode.SUCCESS, productId);
    }

    public GetProductsResponse getProducts()
    {
        return new GetProductsResponse(ExchangeResponseCode.SUCCESS, productBySymbol.values().stream().toList());
    }

    public Product getProduct(final String productSymbol)
    {
        return productBySymbol.get(productSymbol);
    }

    public Asset getAsset(final String assetSymbol)
    {
        return assetBySymbol.get(assetSymbol);
    }

    public Asset getAsset(final long assetId)
    {
        return assetByAssetId.get(assetId);
    }

    public GetAssetsResponse getAssets()
    {
        return new GetAssetsResponse(ExchangeResponseCode.SUCCESS, assetByAssetId.values().stream().toList());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ProductService that = (ProductService)o;

        return new EqualsBuilder()
            .append(currentAssetId, that.currentAssetId)
            .append(currentProductId, that.currentProductId)
            .append(assetBySymbol, that.assetBySymbol)
            .append(assetByAssetId, that.assetByAssetId)
            .append(productBySymbol, that.productBySymbol)
            .isEquals();
    }

    @Override
    public String toString()
    {
        return "ProductService{" +
            "currentAssetId=" + currentAssetId +
            ", currentProductId=" + currentProductId +
            ", assetBySymbol=" + assetBySymbol +
            ", assetByAssetId=" + assetByAssetId +
            ", productBySymbol=" + productBySymbol +
            '}';
    }

    public static class Codec implements Stateful<ProductService>
    {

        @Override
        public ProductService decodeState(ExchangeStateDecoder exchangeStateDecoder)
        {
            final Object2ObjectHashMap<String, Asset> assetBySymbol = new Object2ObjectHashMap<>();
            final Long2ObjectHashMap<Asset> assetByAssetId = new Long2ObjectHashMap<>();
            final MutableLong currentAssetId = new MutableLong();
            exchangeStateDecoder.assets().forEach(asset -> {
                final long assetId = asset.assetId();
                currentAssetId.set(Math.max(assetId, currentAssetId.get()));

                final Asset actualAsset = Asset.builder()
                    .assetId(assetId)
                    .symbol(asset.symbol())
                    .scale(asset.scale())
                    .build();

                assetBySymbol.put(asset.symbol(), actualAsset);
                assetByAssetId.put(assetId, actualAsset);
            });

            final Object2ObjectHashMap<String, Product> products = new Object2ObjectHashMap<>();
            final MutableLong currentProductId = new MutableLong();
            exchangeStateDecoder.products().forEach(productDecoder -> {
                long productId = productDecoder.productId();
                currentProductId.set(Math.max(productId, currentProductId.get()));

                final Product product = Product.builder()
                    .productId(productId)
                    .baseAsset(Asset.builder()
                        .assetId(productDecoder.baseAssetId())
                        .symbol(productDecoder.baseSymbol())
                        .scale(productDecoder.baseScale())
                        .build())
                    .counterAsset(Asset.builder()
                        .assetId(productDecoder.counterAssetId())
                        .symbol(productDecoder.counterSymbol())
                        .scale(productDecoder.counterScale())
                        .build())
                    .makerFee(productDecoder.makerFee())
                    .takerFee(productDecoder.takerFee())
                    .build();

                products.put(product.getSymbol(), product);
            });

            return new ProductService(currentAssetId, currentProductId, assetBySymbol, assetByAssetId, products);
        }

        @Override
        public void encodeState(final ProductService productService, final ExchangeStateEncoder exchangeStateEncoder)
        {
            final Object2ObjectHashMap<String, Asset> assets = productService.assetBySymbol;
            final ExchangeStateEncoder.AssetsEncoder assetsEncoder = exchangeStateEncoder.assetsCount(assets.size());

            assets.forEach((assetSymbol, asset) -> assetsEncoder.next()
                .assetId(asset.getAssetId())
                .symbol(asset.getSymbol())
                .scale(asset.getScale()));

            final Object2ObjectHashMap<String, Product> products = productService.productBySymbol;
            final ExchangeStateEncoder.ProductsEncoder productsEncoder = exchangeStateEncoder.productsCount(products.size());

            products.forEach((productSymbol, product) -> {
                Asset baseAsset = product.getBaseAsset();
                Asset counterAsset = product.getCounterAsset();
                productsEncoder.next()
                    .productId(product.getProductId())
                    .baseAssetId(baseAsset.getAssetId())
                    .baseSymbol(baseAsset.getSymbol())
                    .baseScale(baseAsset.getScale())
                    .counterAssetId(counterAsset.getAssetId())
                    .counterSymbol(counterAsset.getSymbol())
                    .counterScale(counterAsset.getScale())
                    .makerFee(product.getMakerFee())
                    .takerFee(product.getTakerFee());
            });
        }
    }
}
