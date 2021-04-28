package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl.button.ButtonWrapperType;

public final class ButtonDataAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BUTTON_DATA_ATTRIBUTE";

    public static final AttributeProperty<ButtonWrapperType> TYPE = new EnumAttributeProperty<>("ButtonWrapperType", ButtonWrapperType.NORMAL);

    public ButtonDataAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.BUTTON_DATA, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TYPE);
    }

    @Override public void update()
    {
    }
}
