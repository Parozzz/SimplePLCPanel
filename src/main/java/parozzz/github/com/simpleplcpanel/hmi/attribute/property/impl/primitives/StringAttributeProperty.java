package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class StringAttributeProperty extends AttributeProperty<String>
{
    public StringAttributeProperty(String key, boolean allowNullValues)
    {
        this(key, "", allowNullValues);
    }

    public StringAttributeProperty(String key, String defaultValue, boolean allowNullValues)
    {
        super(key, defaultValue, allowNullValues);
    }

    @Override
    public Data createData(Attribute attribute)
    {
        return new StringData();
    }

    public class StringData extends AttributeProperty<String>.Data
    {
        protected StringData()
        {
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            if(value != null)
            {
                jsonDataMap.set(key, value);
            }
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            super.setValue(jsonDataMap.getString(key));
        }
    }
}
