package parozzz.github.com.hmi.attribute.impl.address;

import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;

public class ReadAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "READ_ADDRESS_ATTRIBUTE";
    public ReadAddressAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.READ_ADDRESS, ATTRIBUTE_NAME);
    }
}
