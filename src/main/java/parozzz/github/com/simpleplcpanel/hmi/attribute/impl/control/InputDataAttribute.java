package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.impl.InputWrapper;

public final class InputDataAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "INPUT_DATA";

    public static final AttributeProperty<InputWrapper.Type> TYPE = new EnumAttributeProperty<>("Type", InputWrapper.Type.INTEGER, false);

    public static final AttributeProperty<Long> INT_MAX_VALUE = new NumberAttributeProperty<>("IntMaxValue", Long.MAX_VALUE, Number::longValue);
    public static final AttributeProperty<Long> INT_MIN_VALUE = new NumberAttributeProperty<>("IntMinValue", Long.MIN_VALUE, Number::longValue);

    public static final AttributeProperty<Integer> REAL_MAX_DECIMALS = new NumberAttributeProperty<>("RealMaxDecimals", 10, Number::intValue);
    public static final AttributeProperty<Double> REAL_MAX_VALUE = new NumberAttributeProperty<>("RealMaxValue", Double.MAX_VALUE, Number::doubleValue);
    public static final AttributeProperty<Double> REAL_MIN_VALUE = new NumberAttributeProperty<>("RealMinValue", -Double.MAX_VALUE, Number::doubleValue);

    public static final AttributeProperty<Integer> CHARACTER_LIMIT = new NumberAttributeProperty<>("CharLimit", 1, Number::intValue);

    public InputDataAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.INPUT_DATA, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TYPE, INT_MAX_VALUE, INT_MIN_VALUE,
                REAL_MAX_DECIMALS, REAL_MAX_VALUE, REAL_MIN_VALUE,
                CHARACTER_LIMIT);
    }
}
