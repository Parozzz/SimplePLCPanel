package parozzz.github.com.hmi.attribute.impl.address;

import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;

public class WriteAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "WRITE_ADDRESS_ATTRIBUTE";
    public WriteAddressAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.WRITE_ADDRESS, ATTRIBUTE_NAME);
    }
}
