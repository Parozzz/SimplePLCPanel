package parozzz.github.com.simpleplcpanel.hmi.attribute.impl;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;

public final class SizeAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "SIZE_ATTRIBUTE";

    public static final AttributeProperty<Boolean> ADAPT = new BooleanAttributeProperty("Adapt");
    public static final AttributeProperty<Integer> WIDTH = new NumberAttributeProperty<>("Width", 80, Number::intValue);
    public static final AttributeProperty<Integer> HEIGHT = new NumberAttributeProperty<>("Height", 50, Number::intValue);
    public static final AttributeProperty<Integer> PADDING = new NumberAttributeProperty<>("Padding", -2, Number::intValue);

    public SizeAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.SIZE, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(ADAPT, WIDTH, HEIGHT, PADDING);

    }

    @Override
    public void update()
    {

    }
}
