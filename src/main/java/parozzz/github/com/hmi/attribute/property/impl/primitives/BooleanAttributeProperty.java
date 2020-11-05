package parozzz.github.com.hmi.attribute.property.impl.primitives;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

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
    public void serializeInto(Property<Boolean> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        jsonDataMap.set(super.key, value != null && value);
    }

    @Override
    public void deserializeFrom(Property<Boolean> property, JSONDataMap jsonDataMap)
    {
        super.setValue(property, jsonDataMap.getBoolean(super.key));
    }
}
