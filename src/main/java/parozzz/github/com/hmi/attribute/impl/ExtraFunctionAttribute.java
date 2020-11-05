package parozzz.github.com.hmi.attribute.impl;

import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.extra.ControlWrapperExtraFeature;

public class ExtraFunctionAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "EXTRA_FUNCTION_ATTRIBUTE";

    public final static AttributeProperty<ControlWrapperExtraFeature.Type> TYPE = new EnumAttributeProperty<>("Type", ControlWrapperExtraFeature.Type.NONE);
    public final static AttributeProperty<String> PAGE_NAME = new StringAttributeProperty("PageName", "");

    public ExtraFunctionAttribute()
    {
        super(ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TYPE, PAGE_NAME);
    }

    @Override
    public void updateInternals()
    {

    }

    @Override
    public Attribute cloneEmpty()
    {
        return new ExtraFunctionAttribute();
    }
}
