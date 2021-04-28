package parozzz.github.com.simpleplcpanel.hmi.attribute.impl;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.ParsableAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializables;

public final class FontAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "FONT_ATTRIBUTE";

    public static final AttributeProperty<Color> TEXT_COLOR = new ParsableAttributeProperty<>("TextColor", Color.BLACK, JSONSerializables.COLOR);
    public static final EnumAttributeProperty<Pos> TEXT_POSITION = new EnumAttributeProperty<>("TextPosition", Pos.CENTER);
    public static final AttributeProperty<Boolean> UNDERLINE = new BooleanAttributeProperty("Underline");

    public static final AttributeProperty<String> FONT_NAME = new StringAttributeProperty("FontName", Font.getDefault().getName());
    public static final AttributeProperty<Boolean> BOLD_WEIGHT = new BooleanAttributeProperty("Bold");
    public static final AttributeProperty<Boolean> ITALIC_POSTURE = new BooleanAttributeProperty("Italic");
    public static final AttributeProperty<Boolean> STRIKETHROUGH = new BooleanAttributeProperty("Strikethrough");
    public static final AttributeProperty<Integer> FONT_TEXT_SIZE = new NumberAttributeProperty<>("FontTextSize", 16, Number::intValue);

    private Font font;

    public FontAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.FONT, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TEXT_COLOR, TEXT_POSITION, UNDERLINE, FONT_NAME, BOLD_WEIGHT, ITALIC_POSTURE, STRIKETHROUGH, FONT_TEXT_SIZE);
        this.update(); //Have the font not to be null on startup!
    }

    public Font getFont()
    {
        return font;
    }

    @Override
    public void update()
    {
        var fontName = this.getValue(FONT_NAME);
        var fontWeight = this.getValue(BOLD_WEIGHT) ? FontWeight.BOLD : FontWeight.NORMAL;
        var fontPosture = this.getValue(ITALIC_POSTURE) ? FontPosture.ITALIC : FontPosture.REGULAR;
        var fontTextSize = this.getValue(FONT_TEXT_SIZE);
        this.font = Font.font(fontName, fontWeight, fontPosture, fontTextSize);
    }
}
