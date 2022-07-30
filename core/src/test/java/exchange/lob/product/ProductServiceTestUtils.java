package exchange.lob.product;

import java.util.function.LongConsumer;


public class ProductServiceTestUtils
{

    public static Asset addAsset(final ProductService productService, final String symbol, final byte scale, final LongConsumer onAddAsset)
    {
        productService.addAsset(symbol, scale, onAddAsset);
        return productService.assetBySymbol.get(symbol);
    }

    public static void addProduct(
        final ProductService productService,
        final long baseAssetId,
        final long counterAssetId,
        final long makerFee,
        final long takerFee
    )
    {
        productService.addProduct(baseAssetId, counterAssetId, makerFee, takerFee);
    }
}
