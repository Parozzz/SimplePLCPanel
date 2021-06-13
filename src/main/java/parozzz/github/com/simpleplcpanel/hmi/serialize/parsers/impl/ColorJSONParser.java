package parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.impl;

import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;
import parozzz.github.com.simpleplcpanel.util.Util;

public final class ColorJSONParser extends SuppliedJSONObjectParser<Color>
{
    public ColorJSONParser()
    {
        super("JavaFX.Color", Color.class);
    }

    @Override
    public String serialize(Color color)
    {
        return createHEXString(color == null ? Color.WHITE : color);
    }

    @Override
    public Color create(Object object)
    {
        if(object instanceof String)
        {
            var string = (String) object;
            if(string.startsWith("#") && string.length() == 9)
            {
                var webString = string.substring(0, 7);
                var opacityString = string.substring(7);

                var opacity = Util.parseInt(opacityString, 100, 16);
                return Color.web(webString, Math.max(opacity, 100) / 100d);
            }
        }

        return Color.WHITE;
    }

    private String createHEXString(Color color)
    {
        var intOpacity = (int) (color.getOpacity() * 100d);

        return String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                intOpacity);
    }
}
