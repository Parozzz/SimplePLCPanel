package parozzz.github.com.hmi.attribute.impl;

import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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

public class BaseAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BASE_ATTRIBUTE";

    public static final AttributeProperty<Color> TEXT_COLOR = new ParsableAttributeProperty<>("TextColor", Color.BLACK, JSONSerializables.COLOR);
    public static final EnumAttributeProperty<Pos> TEXT_POSITION = new EnumAttributeProperty<>("TextPosition", Pos.CENTER);
    public static final AttributeProperty<Boolean> UNDERLINE = new BooleanAttributeProperty("Underline");

    public static final AttributeProperty<String> FONT_NAME = new StringAttributeProperty("FontName", Font.getDefault().getName());
    public static final AttributeProperty<Boolean> BOLD_WEIGHT = new BooleanAttributeProperty("Bold");
    public static final AttributeProperty<Boolean> ITALIC_POSTURE = new BooleanAttributeProperty("Italic");
    public static final AttributeProperty<Integer> FONT_TEXT_SIZE = new NumberAttributeProperty<>("FontTextSize", 16, Number::intValue);

    public static final AttributeProperty<Boolean> ADAPT = new BooleanAttributeProperty("Adapt");
    public static final AttributeProperty<Integer> WIDTH = new NumberAttributeProperty<>("Width", 80, Number::intValue);
    public static final AttributeProperty<Integer> HEIGHT = new NumberAttributeProperty<>("Height", 50, Number::intValue);

    private Font font;

    public BaseAttribute()
    {
        super(ATTRIBUTE_NAME);

        super.getAttributePropertyManager().addAll(TEXT_COLOR, TEXT_POSITION, UNDERLINE, FONT_NAME, BOLD_WEIGHT, ITALIC_POSTURE,
                FONT_TEXT_SIZE, ADAPT, WIDTH, HEIGHT);

    }

    public Font getFont()
    {
        return font;
    }

    @Override
    public void updateInternals()
    {
        var fontName = this.getValue(FONT_NAME);
        var fontWeight = this.getValue(BOLD_WEIGHT) ? FontWeight.BOLD : FontWeight.NORMAL;
        var fontPosture = this.getValue(ITALIC_POSTURE) ? FontPosture.ITALIC : FontPosture.REGULAR;
        var fontTextSize = this.getValue(FONT_TEXT_SIZE);
        this.font = Font.font(fontName, fontWeight, fontPosture, fontTextSize);
    }

    @Override
    public BaseAttribute cloneEmpty()
    {
        return new BaseAttribute();
    }
}
