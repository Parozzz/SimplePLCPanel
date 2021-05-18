package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

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
    public Data<T> createData(Attribute attribute)
    {
        return new FunctionData();
    }

    public class FunctionData extends AttributeProperty.Data<T>
    {
        protected FunctionData()
        {
            super(FunctionAttributeProperty.this);
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                var parsedObject = serializeParseFunction.apply(value);
                if(parsedObject != null)
                {
                    jsonDataMap.set(key, parsedObject);
                }
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            var deserializedValue = deserializeFunction.apply(jsonDataMap, key);
            super.setValue(deserializedValue);
        }
    }
}
