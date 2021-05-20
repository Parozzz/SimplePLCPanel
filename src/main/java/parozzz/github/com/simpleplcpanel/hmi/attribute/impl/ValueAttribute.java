package parozzz.github.com.simpleplcpanel.hmi.attribute.impl;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediateType;

public class ValueAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "VALUE_ATTRIBUTE";

    public static final AttributeProperty<ValueIntermediateType<?>> INTERMEDIATE_TYPE =
            new FunctionAttributeProperty<>("IntermediateType", ValueIntermediateType.INTEGER,
                    ValueIntermediateType::getName,
                    (jsonDataMap, key) -> ValueIntermediateType.getByName(jsonDataMap.getString(key)), false);

    public static final AttributeProperty<Double> MULTIPLY_BY = new NumberAttributeProperty<>("DivideBy", 1d, Number::doubleValue);
    public static final AttributeProperty<Double> OFFSET = new NumberAttributeProperty<>("Offset", 0d, Number::doubleValue);

    public ValueAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.VALUE, ATTRIBUTE_NAME);

        this.getAttributePropertyManager().addAll(INTERMEDIATE_TYPE, MULTIPLY_BY, OFFSET);
    }

    @Override public void update()
    {

    }
}
