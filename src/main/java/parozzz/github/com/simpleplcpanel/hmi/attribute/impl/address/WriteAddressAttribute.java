package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.CommunicationTagAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;

public class WriteAddressAttribute extends AddressAttribute
{
    public static final AttributeProperty<CommunicationTag> WRITE_TAG =
        new CommunicationTagAttributeProperty("WriteTag", false);

    public static final String ATTRIBUTE_NAME = "WRITE_ADDRESS";
    public WriteAddressAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.WRITE_ADDRESS, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(WRITE_TAG);
    }

    @Override
    public AttributeProperty<CommunicationTag> getTagAttributeProperty()
    {
        return WRITE_TAG;
    }
}
