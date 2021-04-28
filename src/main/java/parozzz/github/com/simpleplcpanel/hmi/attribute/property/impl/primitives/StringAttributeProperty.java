package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
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
}
