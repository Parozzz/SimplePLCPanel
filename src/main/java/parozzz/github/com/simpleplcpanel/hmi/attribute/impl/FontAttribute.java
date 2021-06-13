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
    public static final String ATTRIBUTE_NAME = "FONT";

    public static final AttributeProperty<Color> COLOR = new ParsableAttributeProperty<>("Color", Color.BLACK, JSONSerializables.COLOR, false);
    public static final EnumAttributeProperty<Pos> POSITION = new EnumAttributeProperty<>("Position", Pos.CENTER, false);
    public static final AttributeProperty<Boolean> UNDERLINE = new BooleanAttributeProperty("Underline");

    public static final AttributeProperty<String> FONT_NAME = new StringAttributeProperty("FontName", Font.getDefault().getName(), false);
    public static final AttributeProperty<Boolean> BOLD = new BooleanAttributeProperty("Bold");
    public static final AttributeProperty<Boolean> ITALIC = new BooleanAttributeProperty("Italic");
    public static final AttributeProperty<Boolean> STRIKETHROUGH = new BooleanAttributeProperty("Strikethrough");
    public static final AttributeProperty<Integer> TEXT_SIZE = new NumberAttributeProperty<>("TextSize", 16, Number::intValue);

    private Font font;

    public FontAttribute(AttributeMap attributeMap)
    {
        super(attributeMap, AttributeType.FONT, ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(COLOR, POSITION, UNDERLINE, FONT_NAME, BOLD, ITALIC, STRIKETHROUGH, TEXT_SIZE);
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
        var fontWeight = this.getValue(BOLD) ? FontWeight.BOLD : FontWeight.NORMAL;
        var fontPosture = this.getValue(ITALIC) ? FontPosture.ITALIC : FontPosture.REGULAR;
        var fontTextSize = this.getValue(TEXT_SIZE);
        this.font = Font.font(fontName, fontWeight, fontPosture, fontTextSize);

        super.update(); //This will call the Update Runnables
    }
}
