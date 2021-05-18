package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class StringAttributeProperty extends AttributeProperty<String>
{
    public StringAttributeProperty(String key)
    {
        this(key, "");
    }

    public StringAttributeProperty(String key, String defaultValue)
    {
        super(key, defaultValue);
    }

    @Override
    public Data<String> createData(Attribute attribute)
    {
        return new StringData();
    }

    /*
    @Override
    public void serializeInto(Property<String> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            jsonDataMap.set(super.key, value);
        }
    }

    @Override
    public void deserializeFrom(Property<String> property, JSONDataMap jsonDataMap)
    {
        super.setValue(property, jsonDataMap.getString(super.key));
    }
*/
    public class StringData extends AttributeProperty.Data<String>
    {
        protected StringData()
        {
            super(StringAttributeProperty.this);
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
