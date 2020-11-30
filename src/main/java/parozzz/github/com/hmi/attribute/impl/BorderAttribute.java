package parozzz.github.com.hmi.attribute.impl;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.ParsableAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.serialize.JSONSerializables;

import javax.naming.ldap.Control;

public class BorderAttribute extends Attribute
{
    public static final String ATTRIBUTE_NAME = "BORDER_ATTRIBUTE";

    public enum StrokeStyle
    {
        SOLID(BorderStrokeStyle.SOLID),
        DASHED(BorderStrokeStyle.DASHED),
        DOTTED(BorderStrokeStyle.DOTTED);

        private final BorderStrokeStyle borderStrokeStyle;
        StrokeStyle(BorderStrokeStyle borderStrokeStyle)
        {
            this.borderStrokeStyle = borderStrokeStyle;
        }
    }

    public static final AttributeProperty<Color> COLOR = new ParsableAttributeProperty<>("Color", Color.BLACK, JSONSerializables.COLOR);
    public static final AttributeProperty<Integer> WIDTH = new NumberAttributeProperty<>("Width", 3, Number::intValue);
    public static final AttributeProperty<StrokeStyle> STROKE_STYLE = new EnumAttributeProperty<>("StrokeStyle", StrokeStyle.SOLID);
    public static final AttributeProperty<Integer> CORNER_RADII = new NumberAttributeProperty<>("CornerRadii", 0, Number::intValue);

    private Border border;

    public BorderAttribute(ControlWrapper<?> controlWrapper)
    {
        super(controlWrapper, ATTRIBUTE_NAME, BorderAttribute::new);

        super.getAttributePropertyManager().addAll(COLOR, WIDTH, STROKE_STYLE, CORNER_RADII);
    }

    public Border getBorder()
    {
        return border;
    }

    @Override
    public void update()
    {
        var borderColor = this.getValue(COLOR);
        var borderWidth = this.getValue(WIDTH);
        var strokeStyle = this.getValue(STROKE_STYLE);
        var cornerRadii = this.getValue(CORNER_RADII);
        this.border = new Border(
                new BorderStroke(borderColor, strokeStyle.borderStrokeStyle, new CornerRadii(cornerRadii), new BorderWidths(borderWidth))
        );
    }
}