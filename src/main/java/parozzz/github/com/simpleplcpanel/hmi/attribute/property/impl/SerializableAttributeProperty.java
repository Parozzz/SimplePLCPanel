package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class SerializableAttributeProperty<T extends JSONSerializable> extends AttributeProperty<T>
{
    public SerializableAttributeProperty(String key, T defaultValue)
    {
        super(key, defaultValue);
    }

    @Override
    public void serializeInto(Property<T> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            jsonDataMap.set(super.key, value.serialize());
        }
    }

    @Override
    public void deserializeFrom(Property<T> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            var valueJSONDataMap = jsonDataMap.getMap(super.key);
            if(valueJSONDataMap != null)
            {
                value.deserialize(valueJSONDataMap);
            }
        }
    }
}
