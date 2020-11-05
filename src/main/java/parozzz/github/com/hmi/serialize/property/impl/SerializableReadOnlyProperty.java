package parozzz.github.com.hmi.serialize.property.impl;

import javafx.beans.property.ReadOnlyProperty;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.hmi.serialize.property.SerializableProperty;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SerializableReadOnlyProperty<T, H> extends SerializableProperty<T>
{
    private final ReadOnlyProperty<H> property;
    private final Function<H, T> conversionFunction;
    private final BiFunction<JSONDataMap, String, T> getterFunction;
    private final Consumer<T> setterConsumer;
/*
    public <M> SerializableReadOnlyProperty(String key, ReadOnlyProperty<H> property,
                                            Consumer<T> setterConsumer, Function<H, T> conversionFunction,
                                            BiFunction<JSONDataMap, String, M> middleGetterFunction, Function<M, T> middleFunction)
    {
        super(key);

        this.property = property;
        this.setterConsumer = setterConsumer;
        this.conversionFunction = conversionFunction;
        getterFunction = (tJSONDataMap, tKey) ->
        {
            var middleValue = middleGetterFunction.apply(tJSONDataMap, tKey);
            return middleFunction.apply(middleValue);
        };
    }*/

    public SerializableReadOnlyProperty(String key, ReadOnlyProperty<H> property,
                                        Consumer<T> setterConsumer, Function<H, T> conversionFunction,
                                        BiFunction<JSONDataMap, String, H> middleGetterFunction)
    {
        super(key);

        this.property = property;
        this.setterConsumer = setterConsumer;
        this.conversionFunction = conversionFunction;
        this.getterFunction = (tJSONDataMap, tKey) ->
        {
            var middleValue = middleGetterFunction.apply(tJSONDataMap, tKey);
            if(middleValue == null)
            {
                return null;
            }

            return conversionFunction.apply(middleValue);
        };
    }

    @Override
    public void set(JSONDataMap jsonDataMap)
    {
        if (jsonDataMap == null)
        {
            return;
        }

        var value = property.getValue();
        if (value != null)
        {
            var convertedValue = conversionFunction.apply(value);
            if(convertedValue != null)
            {
                jsonDataMap.set(key, convertedValue);
            }
        }
    }

    @Override
    public void load(JSONDataMap jsonDataMap)
    {
        if (jsonDataMap == null)
        {
            return;
        }

        var value = getterFunction.apply(jsonDataMap, key);
        if (value != null)
        {
            setterConsumer.accept(value);
        }
    }
}
