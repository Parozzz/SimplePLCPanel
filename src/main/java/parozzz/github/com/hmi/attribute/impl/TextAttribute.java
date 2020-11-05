package parozzz.github.com.hmi.attribute.impl;

import javafx.scene.text.TextAlignment;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;

public class TextAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "TEXT_ATTRIBUTE";

    public static final StringAttributeProperty TEXT = new StringAttributeProperty("Text", "DEFAULT\nTEXT");
    public static final EnumAttributeProperty<TextAlignment> TEXT_ALIGNMENT = new EnumAttributeProperty<>("TextAlignment", TextAlignment.CENTER);
    public static final NumberAttributeProperty<Integer> LINE_SPACING = new NumberAttributeProperty<>("LineSpacing", 0, Number::intValue);

    public TextAttribute()
    {
        super(ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TEXT, TEXT_ALIGNMENT, LINE_SPACING);
    }

    @Override
    public void updateInternals()
    {

    }

    @Override
    public TextAttribute cloneEmpty()
    {
        //No attribute initialization because while cloned they are added afterwards
        return new TextAttribute();
    }
}
