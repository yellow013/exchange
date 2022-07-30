package exchange.lob.acceptance;

import exchange.lob.acceptance.dsl.Util;
import exchange.lob.user.UserService;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;

public class TestStorage
{
    private final Map<String, String> usernameAliasMap = new HashMap<>();
    private final Map<String, String> assetAliasMap = new HashMap<>();
    private final Map<String, Byte> assetToScaleMap = new HashMap<>();
    private final Map<String, String> productAliasMap = new HashMap<>();

    private final Map<String, Long> userToUserIdMap = new HashMap<>();
    private final Map<String, Long> assetToAssetIdMap = new HashMap<>();
    private final Map<String, Long> productToProductIdMap = new HashMap<>();

    public TestStorage()
    {
        userToUserIdMap.put(UserService.EXCHANGE_USERNAME, UserService.EXCHANGE_USER_ID);
        usernameAliasMap.put(UserService.EXCHANGE_USERNAME, UserService.EXCHANGE_USERNAME);
    }

    public String storeUser(final String username)
    {
        if (usernameAliasMap.containsKey(username))
        {
            return usernameAliasMap.get(username);
        }
        final String systemUsername = Util.randomizeAndTruncateUsername(username);
        usernameAliasMap.put(username, systemUsername);
        return systemUsername;
    }

    public String getSystemUsername(final String username)
    {
        return usernameAliasMap.get(username);
    }

    public void storeAsset(final String symbol, final String systemSymbol, final byte scale)
    {
        assetAliasMap.put(symbol, systemSymbol);
        assetToScaleMap.put(symbol, scale);
    }

    public static String generateSystemSymbolFor(final String symbol)
    {
        return RandomStringUtils.random(symbol.length(), true, true).toUpperCase();
    }

    public String getSystemAsset(final String symbol)
    {
        return assetAliasMap.get(symbol);
    }

    public void storeProduct(final String productSymbol, final String systemProductSymbol)
    {
        productAliasMap.put(productSymbol, systemProductSymbol);
    }

    public String getSystemProduct(final String product)
    {
        if (isLiteral(product))
        {
            return product.replace("<", "").replace(">", "");
        }
        return productAliasMap.get(product);
    }

    private static boolean isLiteral(final String product)
    {
        return product.startsWith("<") && product.endsWith(">");
    }

    public void storeAssetId(final String systemAssetSymbol, final long assetId)
    {
        assetToAssetIdMap.put(systemAssetSymbol, assetId);
    }

    public long getAssetId(final String systemAssetSymbol)
    {
        return assetToAssetIdMap.get(systemAssetSymbol);
    }

    public long getAssetIdByAlias(final String alias)
    {
        return assetToAssetIdMap.get(getSystemAsset(alias));
    }

    public byte getAssetScale(final String asset)
    {
        return assetToScaleMap.get(asset);
    }

    public void storeProductId(final String systemProductSymbol, final long productId)
    {
        productToProductIdMap.put(systemProductSymbol, productId);
    }

    public long getProductId(final String systemProduct)
    {
        return productToProductIdMap.get(systemProduct);
    }

    public void storeUserId(final String username, final long userId)
    {
        userToUserIdMap.put(username, userId);
    }

    public long getUserId(final String username)
    {
        return userToUserIdMap.get(username);
    }

    public long getUserIdByAlias(final String usernameAlias)
    {
        return userToUserIdMap.get(usernameAliasMap.get(usernameAlias));
    }
}
