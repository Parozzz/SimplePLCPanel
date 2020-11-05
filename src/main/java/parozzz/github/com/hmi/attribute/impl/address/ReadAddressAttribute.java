package parozzz.github.com.hmi.attribute.impl.address;

public class ReadAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "READ_ADDRESS_ATTRIBUTE";
    public ReadAddressAttribute()
    {
        super(ATTRIBUTE_NAME);
    }

    @Override
    public AddressAttribute cloneEmpty()
    {
        return new ReadAddressAttribute();
    }
}
