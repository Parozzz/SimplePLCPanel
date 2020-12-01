package parozzz.github.com.hmi.attribute.impl;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;

public final class ChangePageAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "CHANGE_PAGE_ATTRIBUTE";

    public final static AttributeProperty<Boolean> ENABLED = new BooleanAttributeProperty("Enabled", false);
    public final static AttributeProperty<String> PAGE_NAME = new StringAttributeProperty("PageName", "");

    public ChangePageAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.CHANGE_PAGE, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(ENABLED, PAGE_NAME);
    }

    @Override
    public void update()
    {

    }
}
