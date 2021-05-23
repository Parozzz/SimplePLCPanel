package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;

public abstract class AddressAttribute extends Attribute
{
    public AddressAttribute(AttributeMap attributeMap,
            AttributeType<? extends AddressAttribute> attributeType, String name)
    {
        super(attributeMap, attributeType, name);
    }

    @Nullable
    public CommunicationTag getCommunicationTag()
    {
        return this.getValue(this.getTagAttributeProperty());
    }

    public void setCommunicationTag(CommunicationTag tag)
    {
        this.setValue(this.getTagAttributeProperty(), tag);
    }

    public abstract AttributeProperty<CommunicationTag> getTagAttributeProperty();
}
