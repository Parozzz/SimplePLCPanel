package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class SerializableAttributeProperty<T extends JSONSerializable>
        extends AttributeProperty<T>
{
    public SerializableAttributeProperty(String key, T defaultValue)
    {
        super(key, defaultValue);
    }

    @Override
    public Data<T> createData(Attribute attribute)
    {
        return new SerializableData();
    }
/*
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
    }*/

    public class SerializableData extends AttributeProperty.Data<T>
    {
        protected SerializableData()
        {
            super(SerializableAttributeProperty.this);
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                jsonDataMap.set(key, value.serialize());
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                var valueJSONDataMap = jsonDataMap.getMap(key);
                if(valueJSONDataMap != null)
                {
                    value.deserialize(valueJSONDataMap);
                }
            }
        }
    }
}
