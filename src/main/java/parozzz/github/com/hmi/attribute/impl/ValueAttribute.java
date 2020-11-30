package parozzz.github.com.hmi.attribute.impl;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediateType;

public class ValueAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "VALUE_ATTRIBUTE";

    public static final AttributeProperty<ValueIntermediateType<?>> INTERMEDIATE_TYPE =
            new FunctionAttributeProperty<>("IntermediateType", ValueIntermediateType.INTEGER,
                    ValueIntermediateType::getName,
                    (jsonDataMap, key) -> ValueIntermediateType.getByName(jsonDataMap.getString(key)));

    public static final AttributeProperty<Double> MULTIPLY_BY = new NumberAttributeProperty<>("DivideBy", 1d, Number::doubleValue);
    public static final AttributeProperty<Double> OFFSET = new NumberAttributeProperty<>("Offset", 0d, Number::doubleValue);

    public ValueAttribute(ControlWrapper<?> controlWrapper)
    {
        super(controlWrapper, ATTRIBUTE_NAME, ValueAttribute::new);

        this.getAttributePropertyManager().addAll(INTERMEDIATE_TYPE, MULTIPLY_BY, OFFSET);
    }

    @Override public void update()
    {

    }
}
