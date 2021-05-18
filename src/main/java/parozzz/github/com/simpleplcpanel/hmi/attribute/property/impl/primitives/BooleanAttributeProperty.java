package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class BooleanAttributeProperty extends AttributeProperty<Boolean>
{
    public BooleanAttributeProperty(String key)
    {
        this(key, false);
    }

    public BooleanAttributeProperty(String key, boolean defaultValue)
    {
        super(key, defaultValue);
    }

    @Override
    public Data<Boolean> createData(Attribute attribute)
    {
        return new BooleanData();
    }

    public class BooleanData extends AttributeProperty.Data<Boolean>
    {
        protected BooleanData()
        {
            super(BooleanAttributeProperty.this);
        }

        @Override
        public void serializeInto(JSONDataMap jsonDataMap)
        {
            var value = property.getValue();
            jsonDataMap.set(key, value != null && value);
        }

        @Override
        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            super.setValue(jsonDataMap.getBoolean(key));
        }
    }
}
