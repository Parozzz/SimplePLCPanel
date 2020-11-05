package parozzz.github.com.hmi.serialize.parsers.impl;

import javafx.scene.paint.Color;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.hmi.serialize.parsers.SuppliedJSONObjectParser;

public final class ColorJSONParser extends SuppliedJSONObjectParser<Color>
{
    public ColorJSONParser()
    {
        super("JavaFX.Color", Color.class);
    }

    @Override
    public JSONDataMap serialize(Color color)
    {
        var jsonDataMap = new JSONDataMap();
        if(color == null)
        {
            return jsonDataMap;
        }

        super.setIdentifier(jsonDataMap);

        jsonDataMap.set("Red", color.getRed());
        jsonDataMap.set("Green", color.getGreen());
        jsonDataMap.set("Blue", color.getBlue());
        jsonDataMap.set("Opacity", color.getOpacity());

        return jsonDataMap;
    }

    @Override
    public Color create(JSONDataMap jsonDataMap)
    {
        if(!super.hasIdentifier(jsonDataMap))
        {
            return null;
        }

        var red = jsonDataMap.getNumber("Red").doubleValue();
        var green = jsonDataMap.getNumber("Green").doubleValue();
        var blue = jsonDataMap.getNumber("Blue").doubleValue();
        var opacity = jsonDataMap.getNumber("Opacity").doubleValue();

        return new Color(red, green, blue, opacity);
    }
}
