package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.impl.ButtonWrapper;

public final class ButtonDataAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BUTTON_DATA";

    public static final AttributeProperty<ButtonWrapper.Type> TYPE = new EnumAttributeProperty<>("Type", ButtonWrapper.Type.NORMAL, false);

    public ButtonDataAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.BUTTON_DATA, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TYPE);
    }
}
