package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class SerializableAttributeProperty<T extends JSONSerializable>
        extends AttributeProperty<T>
{
    public SerializableAttributeProperty(String key, T defaultValue, boolean allowNullValues)
    {
        super(key, defaultValue, allowNullValues);
    }

    @Override
    public Data createData(Attribute attribute)
    {
        return new SerializableData();
    }

    public class SerializableData extends AttributeProperty<T>.Data
    {
        protected SerializableData()
        {
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
