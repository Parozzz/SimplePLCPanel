package parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.SerializableProperty;

public class SerializableParsedProperty<T> extends SerializableProperty<T>
{
    private final Property<T> property;
    private final SuppliedJSONObjectParser<T> suppliedJSONObjectParser;
    public SerializableParsedProperty(String key, Property<T> property,
                                      SuppliedJSONObjectParser<T> suppliedJSONObjectParser)
    {
        super(key);

        this.property = property;
        this.suppliedJSONObjectParser = suppliedJSONObjectParser;
    }

    @Override
    public void set(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null)
        {
            return;
        }

        var value = property.getValue();
        if(value == null)
        {
            return;
        }

        var serializedPropertyJSONDataMap = suppliedJSONObjectParser.serialize(value);
        if(serializedPropertyJSONDataMap != null)
        {
            jsonDataMap.set(key, serializedPropertyJSONDataMap);
        }
    }

    @Override
    public void load(JSONDataMap jsonDataMap)
    {
        if(jsonDataMap == null)
        {
            return;
        }

        var suppliedJSONObjectDataMap = jsonDataMap.getMap(key);
        if(suppliedJSONObjectDataMap != null)
        {
            var value = suppliedJSONObjectParser.create(suppliedJSONObjectDataMap);
            if(value != null)
            {
                property.setValue(value);
            }
        }

    }
}
