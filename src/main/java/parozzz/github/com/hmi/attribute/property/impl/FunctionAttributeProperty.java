package parozzz.github.com.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionAttributeProperty<T> extends AttributeProperty<T>
{
    private final Function<T, Object> serializeParseFunction;
    private final BiFunction<JSONDataMap, String, T> deserializeFunction;
    public FunctionAttributeProperty(String key, T defaultValue,
            Function<T, Object> serializeParseFunction,
            BiFunction<JSONDataMap, String, T> deserializeFunction)
    {
        super(key, defaultValue);

        this.serializeParseFunction = serializeParseFunction;
        this.deserializeFunction = deserializeFunction;
    }

    @Override
    public void serializeInto(Property<T> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            var parsedObject = serializeParseFunction.apply(value);
            if(parsedObject != null)
            {
                jsonDataMap.set(super.key, parsedObject);
            }
        }
    }

    @Override
    public void deserializeFrom(Property<T> property, JSONDataMap jsonDataMap)
    {
        super.setValue(property, deserializeFunction.apply(jsonDataMap, super.key));
    }
}
