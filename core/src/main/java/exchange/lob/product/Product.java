package exchange.lob.product;

import exchange.lob.domain.Side;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Objects;

import static exchange.lob.domain.Side.ASK;
import static exchange.lob.domain.Side.BID;
import static exchange.lob.math.Quantiser.toMajorUnits;
import static exchange.lob.math.Quantiser.toMinorUnits;


public class Product implements Encodable
{
    public static final long BPS_SCALE = 10000L;
    private long productId;
    private final Asset baseAsset;
    private final Asset counterAsset;
    private long makerFee;
    private long takerFee;

    public Product(final long productId, final Asset baseAsset, final Asset counterAsset, final long makerFee, final long takerFee)
    {
        this.productId = productId;
        this.baseAsset = baseAsset;
        this.counterAsset = counterAsset;
        this.makerFee = makerFee;
        this.takerFee = takerFee;
    }

    public static ProductBuilder builder()
    {
        return new ProductBuilder();
    }

    public String getSymbol()
    {
        return baseAsset.getSymbol() + counterAsset.getSymbol();
    }

    /**
     * Converts base asset minor units amount to counter asset minor units amount at a given price
     *
     * @param price  price of a deal
     * @param amount amount to deal
     * @return counter asset amount in minor units
     */
    public long deal(final long price, final long amount)
    {
        final double majorPrice = toMajorUnits(price, counterAsset.getScale());
        final double majorAmount = toMajorUnits(amount, baseAsset.getScale());
        return toMinorUnits(majorPrice * majorAmount, counterAsset.getScale(), Math::ceil);
    }

    /**
     * Computes the asset the maker is to receive as a result of making the trade on a given side
     *
     * @param takerSide aggressor order side
     * @return asset ID
     */
    public long getMakerAssetId(final Side takerSide)
    {
        return switch (takerSide)
            {
                case BID -> counterAsset.getAssetId();
                case ASK -> baseAsset.getAssetId();
                case NULL_VAL -> -1L;
            };
    }

    /**
     * Computes the asset the taker is to receive as a result of given aggressive side trade
     *
     * @param takerSide aggressor order side
     * @return asset ID
     */
    public long getTakerAssetId(Side takerSide)
    {
        return switch (takerSide)
            {
                case BID -> baseAsset.getAssetId();
                case ASK -> counterAsset.getAssetId();
                case NULL_VAL -> -1L;
            };
    }

    /**
     * Computes amount maker will receive as a result of providing liquidity
     * Amount can be denominated in base or counter asset
     *
     * @param takerSide aggressor order side
     * @param price     match price
     * @param amount    match amount
     * @return asset amount
     */
    public long make(final Side takerSide, final long price, final long amount)
    {
        return takerSide == BID ? deal(price, amount) : amount;
    }

    /**
     * Computes amount taker will receive as a result of taking liquidity
     * Amount can be denominated in base or counter asset
     *
     * @param takerSide aggressor order side
     * @param price     match price
     * @param amount    match amount
     * @return asset amount
     */
    public long take(final Side takerSide, final long price, final long amount)
    {
        return takerSide == ASK ? deal(price, amount) : amount;
    }

    public long calculateMakerFees(final long takerReceipts)
    {
        return makerFee * takerReceipts / BPS_SCALE;
    }

    public long calculateTakerFees(final long takerReceipts)
    {
        return takerFee * takerReceipts / BPS_SCALE;
    }

    public long getCancellationAssetId(final Side side)
    {
        return getMakerAssetId(side);
    }

    public long cancel(final Side side, final long price, final long amount)
    {
        return make(side, price, amount);
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(productId);
        baseAsset.encode(bufferEncoder);
        counterAsset.encode(bufferEncoder);
        bufferEncoder.encode(makerFee);
        bufferEncoder.encode(takerFee);
    }

    @DecodedBy
    public static Product decode(final BufferDecoder bufferDecoder)
    {
        final long productId = bufferDecoder.decodeLong();
        final Asset baseAsset = Asset.decode(bufferDecoder);
        final Asset counterAsset = Asset.decode(bufferDecoder);
        final long makerFee = bufferDecoder.decodeLong();
        final long takerFee = bufferDecoder.decodeLong();
        return new Product(productId, baseAsset, counterAsset, makerFee, takerFee);
    }

    public long getProductId()
    {
        return productId;
    }

    public Asset getBaseAsset()
    {
        return baseAsset;
    }

    public Asset getCounterAsset()
    {
        return counterAsset;
    }

    public long getMakerFee()
    {
        return makerFee;
    }

    public long getTakerFee()
    {
        return takerFee;
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
        final Product product = (Product)o;
        return productId == product.productId &&
            makerFee == product.makerFee && takerFee == product.takerFee && Objects.equals(baseAsset, product.baseAsset) && Objects.equals(
            counterAsset,
            product.counterAsset
        );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(productId, baseAsset, counterAsset, makerFee, takerFee);
    }

    public String toString()
    {
        return "Product(productId=" + this.getProductId() + ", baseAsset=" + this.getBaseAsset() +
            ", counterAsset=" + this.getCounterAsset() + ", makerFee=" + this.getMakerFee() + ", takerFee=" +
            this.getTakerFee() + ")";
    }

    public void setProductId(long productId)
    {
        this.productId = productId;
    }

    public void setMakerFee(long makerFee)
    {
        this.makerFee = makerFee;
    }

    public void setTakerFee(long takerFee)
    {
        this.takerFee = takerFee;
    }

    public static class ProductBuilder
    {
        private long productId;
        private Asset baseAsset;
        private Asset counterAsset;
        private long makerFee;
        private long takerFee;

        ProductBuilder()
        {
        }

        public ProductBuilder productId(long productId)
        {
            this.productId = productId;
            return this;
        }

        public ProductBuilder baseAsset(Asset baseAsset)
        {
            this.baseAsset = baseAsset;
            return this;
        }

        public ProductBuilder counterAsset(Asset counterAsset)
        {
            this.counterAsset = counterAsset;
            return this;
        }

        public ProductBuilder makerFee(long makerFee)
        {
            this.makerFee = makerFee;
            return this;
        }

        public ProductBuilder takerFee(long takerFee)
        {
            this.takerFee = takerFee;
            return this;
        }

        public Product build()
        {
            return new Product(productId, baseAsset, counterAsset, makerFee, takerFee);
        }

        public String toString()
        {
            return "Product.ProductBuilder(productId=" + this.productId + ", baseAsset=" + this.baseAsset +
                ", counterAsset=" + this.counterAsset + ", makerFee=" + this.makerFee + ", takerFee=" + this.takerFee +
                ")";
        }
    }
}
