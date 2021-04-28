package parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.SerializableProperty;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SerializableSimpleProperty<T> extends SerializableProperty<T>
{
    private final Property<T> property;
    private final BiFunction<JSONDataMap, String, T> getterFunction;

    public <M> SerializableSimpleProperty(String key, Property<T> property,
                                          BiFunction<JSONDataMap, String, M> middleGetterFunction, Function<M, T> middleFunction)
    {
        super(key);

        this.property = property;
        getterFunction = (tJSONDataMap, tKey) ->
        {
            var middleValue = middleGetterFunction.apply(tJSONDataMap, tKey);
            if(middleValue == null)
            {
                return null;
            }

            return middleFunction.apply(middleValue);
        };
    }

    public SerializableSimpleProperty(String key, Property<T> property, BiFunction<JSONDataMap, String, T> getterFunction)
    {
        super(key);

        this.property = property;
        this.getterFunction = getterFunction;
    }

    @Override
    public void set(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null)
        {
            return;
        }

        var value = property.getValue();
        if(value != null)
        {
            jsonDataMap.set(key, value);
        }
    }

    @Override
    public void load(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null)
        {
            return;
        }

        var value = getterFunction.apply(jsonDataMap, key);
        if(value != null)
        {
            property.setValue(value);
        }
    }
}
