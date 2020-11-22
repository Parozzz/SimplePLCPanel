package parozzz.github.com.hmi.attribute.impl;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.ParsableAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.hmi.serialize.JSONSerializables;

public final class SizeAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "SIZE_ATTRIBUTE";

    public static final AttributeProperty<Boolean> ADAPT = new BooleanAttributeProperty("Adapt");
    public static final AttributeProperty<Integer> WIDTH = new NumberAttributeProperty<>("Width", 80, Number::intValue);
    public static final AttributeProperty<Integer> HEIGHT = new NumberAttributeProperty<>("Height", 50, Number::intValue);

    public SizeAttribute()
    {
        super(ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(ADAPT, WIDTH, HEIGHT);

    }

    @Override
    public void updateInternals()
    {

    }

    @Override
    public SizeAttribute cloneEmpty()
    {
        return new SizeAttribute();
    }
}
