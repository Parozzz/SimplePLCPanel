package parozzz.github.com.hmi.attribute.impl.address;

public class WriteAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "WRITE_ADDRESS_ATTRIBUTE";
    public WriteAddressAttribute()
    {
        super(ATTRIBUTE_NAME);
    }

    @Override
    public AddressAttribute cloneEmpty()
    {
        return new WriteAddressAttribute();
    }
}
