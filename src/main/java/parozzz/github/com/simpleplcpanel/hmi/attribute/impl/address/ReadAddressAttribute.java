package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.CommunicationTagAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;

public class ReadAddressAttribute extends AddressAttribute
{
    public static final AttributeProperty<CommunicationTag> READ_TAG =
            new CommunicationTagAttributeProperty("ReadTag", true);

    public static final String ATTRIBUTE_NAME = "READ_ADDRESS_ATTRIBUTE";
    public ReadAddressAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.READ_ADDRESS, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(READ_TAG);
    }

    @Override
    public AttributeProperty<CommunicationTag> getTagAttributeProperty()
    {
        return READ_TAG;
    }
}
