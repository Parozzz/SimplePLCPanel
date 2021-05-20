package parozzz.github.com.simpleplcpanel.hmi.attribute.impl;

import javafx.scene.text.TextAlignment;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.StringAttributeProperty;

public class TextAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "TEXT_ATTRIBUTE";

    public static final StringAttributeProperty TEXT = new StringAttributeProperty("Text", "DEFAULT\nTEXT", false);
    public static final EnumAttributeProperty<TextAlignment> TEXT_ALIGNMENT = new EnumAttributeProperty<>("TextAlignment", TextAlignment.CENTER, false);
    public static final NumberAttributeProperty<Integer> LINE_SPACING = new NumberAttributeProperty<>("LineSpacing", 0, Number::intValue);

    public TextAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.TEXT, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TEXT, TEXT_ALIGNMENT, LINE_SPACING);
    }

    @Override
    public void update()
    {

    }
}
