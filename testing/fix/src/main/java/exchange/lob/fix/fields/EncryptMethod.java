package exchange.lob.fix.fields;

public enum EncryptMethod
{
    NONE(0),
    PKCS(1),
    DES(2),
    PKCS_DES(3),
    PGP_DES(4),
    PGP_DES_MD5(5),
    PEM_DES_MD5(6);

    private final int code;

    EncryptMethod(final int code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return Integer.toString(code);
    }
}
