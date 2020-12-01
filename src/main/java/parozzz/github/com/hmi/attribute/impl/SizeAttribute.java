package parozzz.github.com.hmi.attribute.impl;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;

public final class SizeAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "SIZE_ATTRIBUTE";

    public static final AttributeProperty<Boolean> ADAPT = new BooleanAttributeProperty("Adapt");
    public static final AttributeProperty<Integer> WIDTH = new NumberAttributeProperty<>("Width", 80, Number::intValue);
    public static final AttributeProperty<Integer> HEIGHT = new NumberAttributeProperty<>("Height", 50, Number::intValue);

    public SizeAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.SIZE, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(ADAPT, WIDTH, HEIGHT);

    }

    @Override
    public void update()
    {

    }
}
