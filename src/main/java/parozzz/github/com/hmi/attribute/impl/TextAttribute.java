package parozzz.github.com.hmi.attribute.impl;

import javafx.scene.text.TextAlignment;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

public class TextAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "TEXT_ATTRIBUTE";

    public static final StringAttributeProperty TEXT = new StringAttributeProperty("Text", "DEFAULT\nTEXT");
    public static final EnumAttributeProperty<TextAlignment> TEXT_ALIGNMENT = new EnumAttributeProperty<>("TextAlignment", TextAlignment.CENTER);
    public static final NumberAttributeProperty<Integer> LINE_SPACING = new NumberAttributeProperty<>("LineSpacing", 0, Number::intValue);

    public TextAttribute(ControlWrapper<?> controlWrapper)
    {
        super(controlWrapper, ATTRIBUTE_NAME, TextAttribute::new);

        super.getAttributePropertyManager().addAll(TEXT, TEXT_ALIGNMENT, LINE_SPACING);
    }

    @Override
    public void update()
    {

    }
}
