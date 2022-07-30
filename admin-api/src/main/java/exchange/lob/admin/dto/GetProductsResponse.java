package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetProductsResponse
{
    public final List<Product> products;

    @JsonCreator
    public GetProductsResponse(final @JsonProperty("products") List<Product> products)
    {
        this.products = products;
    }

    public static class Product
    {
        public final long productId;
        public final String symbol;
        public final byte baseScale;
        public final byte counterScale;
        public final long makerFee;
        public final long takerFee;

        @JsonCreator
        public Product(
            final @JsonProperty("productId") long productId,
            final @JsonProperty("symbol") String symbol,
            final @JsonProperty("baseScale") byte baseScale,
            final @JsonProperty("counterScale") byte counterScale,
            final @JsonProperty("makerFee") long makerFee,
            final @JsonProperty("takerFee") long takerFee
        )
        {
            this.productId = productId;
            this.symbol = symbol;
            this.baseScale = baseScale;
            this.counterScale = counterScale;
            this.makerFee = makerFee;
            this.takerFee = takerFee;
        }

        public static Product toResponse(final exchange.lob.product.Product product)
        {
            return new Product(
                product.getProductId(),
                product.getSymbol(),
                product.getBaseAsset().getScale(),
                product.getCounterAsset().getScale(),
                product.getMakerFee(),
                product.getTakerFee()
            );
        }
    }
}
