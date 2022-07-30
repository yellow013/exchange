package exchange.lob.product;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Objects;

public class Asset implements Encodable
{
    private final long assetId;
    private final String symbol;
    private byte scale;

    public Asset(final long assetId, final String symbol, final byte scale)
    {
        this.assetId = assetId;
        this.symbol = symbol;
        this.scale = scale;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(assetId);
        bufferEncoder.encode(symbol);
        bufferEncoder.encode(scale);
    }

    @DecodedBy
    public static Asset decode(final BufferDecoder bufferDecoder)
    {
        final long assetId = bufferDecoder.decodeLong();
        final String symbol = bufferDecoder.decodeString();
        final byte scale = bufferDecoder.decodeByte();
        return new Asset(assetId, symbol, scale);
    }

    public long getAssetId()
    {
        return this.assetId;
    }

    public String getSymbol()
    {
        return this.symbol;
    }

    public byte getScale()
    {
        return this.scale;
    }

    public void setScale(byte scale)
    {
        this.scale = scale;
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
        final Asset asset = (Asset)o;
        return assetId == asset.assetId && scale == asset.scale && Objects.equals(symbol, asset.symbol);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(assetId, symbol, scale);
    }

    @Override
    public String toString()
    {
        return "Asset{" +
            "assetId=" + assetId +
            ", symbol='" + symbol + '\'' +
            ", scale=" + scale +
            '}';
    }

    public static AssetBuilder builder()
    {
        return new AssetBuilder();
    }

    public static class AssetBuilder
    {
        private long assetId;
        private String symbol;
        private byte scale;

        AssetBuilder()
        {
        }

        public AssetBuilder assetId(long assetId)
        {
            this.assetId = assetId;
            return this;
        }

        public AssetBuilder symbol(String symbol)
        {
            this.symbol = symbol;
            return this;
        }

        public AssetBuilder scale(byte scale)
        {
            this.scale = scale;
            return this;
        }

        public Asset build()
        {
            return new Asset(assetId, symbol, scale);
        }

        @Override
        public String toString()
        {
            return "AssetBuilder{" +
                "assetId=" + assetId +
                ", symbol='" + symbol + '\'' +
                ", scale=" + scale +
                '}';
        }
    }
}
