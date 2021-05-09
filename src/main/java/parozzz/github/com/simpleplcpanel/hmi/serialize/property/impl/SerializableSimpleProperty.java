package parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.SerializableProperty;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SerializableSimpleProperty<T> extends SerializableProperty<T>
{
    public static <T> Builder<T> builder(String key, Property<T> property)
    {
        return new Builder<>(key, property);
    }

    private final Property<T> property;
    private final BiFunction<JSONDataMap, String, T> getterFunction;
    private final Function<T, Object> setterFunction;

    private final T loadDefaultValue;

    public  SerializableSimpleProperty(String key, Property<T> property,
            BiFunction<JSONDataMap, String, T> getterFunction, Function<T, Object> setterFunction,
            T loadDefaultValue)
    {
        super(key);

        this.property = property;
        this.getterFunction = getterFunction;
        this.setterFunction = setterFunction;
        this.loadDefaultValue = loadDefaultValue;
    }

    @Override
    public void set(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null)
        {
            return;
        }

        //Setter function is never null. At worst it return the same value!
        var value = property.getValue();
        if(value != null)
        {
            var setObject = setterFunction.apply(value);
            if(setObject != null)
            {
                jsonDataMap.set(key, setObject);
            }
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
        } else if(loadDefaultValue != null)
        {
            property.setValue(loadDefaultValue);
        }
    }

    public static class Builder<T>
    {
        private final String key;
        private final Property<T> property;
        private T loadDefaultValue;
        private BiFunction<JSONDataMap, String, T> getterFunction = (jsonDataMap, s) -> null;
        private Function<T, Object> setterFunction = t -> t; //If not set, return the same object!

        public Builder(String key, Property<T> property)
        {
            this.key = key;
            this.property = property;
        }

        public Builder<T> getterFunction(BiFunction<JSONDataMap, String, T> getterFunction)
        {
            this.getterFunction = getterFunction;
            return this;
        }

        public <M> Builder<T> getterFunction(BiFunction<JSONDataMap, String, M> middleGetterFunction,
                Function<M, T> middleFunction)
        {
            this.getterFunction = (tJSONDataMap, tKey) ->
            {
                var middleValue = middleGetterFunction.apply(tJSONDataMap, tKey);
                if(middleValue == null)
                {
                    return null;
                }

                return middleFunction.apply(middleValue);
            };

            return this;
        }

        public Builder<T> setterFunction(Function<T, Object> setterFunction)
        {
            this.setterFunction = setterFunction;
            return this;
        }

        public Builder<T> loadDefaultValue(T loadDefaultValue)
        {
            this.loadDefaultValue = loadDefaultValue;
            return this;
        }

        public SerializableSimpleProperty<T> build()
        {
            return new SerializableSimpleProperty<>(key, property, getterFunction, setterFunction, loadDefaultValue);
        }
    }
}
