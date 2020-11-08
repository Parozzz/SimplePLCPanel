package parozzz.github.com.hmi.attribute.impl.control;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.impl.button.ButtonWrapperType;

public final class ButtonDataAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BUTTON_DATA_ATTRIBUTE";

    public static final AttributeProperty<ButtonWrapperType> TYPE = new EnumAttributeProperty<>("ButtonWrapperType", ButtonWrapperType.NORMAL);

    public ButtonDataAttribute()
    {
        super(ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TYPE);
    }

    @Override public void updateInternals()
    {
    }

    @Override public Attribute cloneEmpty()
    {
        return new ButtonDataAttribute();
    }
}