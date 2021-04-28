package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;

public class WriteAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "WRITE_ADDRESS_ATTRIBUTE";
    public WriteAddressAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.WRITE_ADDRESS, ATTRIBUTE_NAME);
    }
}
