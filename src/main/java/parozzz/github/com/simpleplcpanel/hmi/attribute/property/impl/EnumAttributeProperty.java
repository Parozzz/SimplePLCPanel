package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class EnumAttributeProperty<E extends Enum<E>> extends AttributeProperty<E>
{
    private final Class<E> enumClass;
    public EnumAttributeProperty(String key, E defaultValue)
    {
        super(key, defaultValue);

        enumClass = defaultValue.getDeclaringClass();
    }

    @Override
    public void serializeInto(Property<E> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            var stringValue = value.name();
            jsonDataMap.set(super.key, value);
        }
    }

    @Override
    public void deserializeFrom(Property<E> property, JSONDataMap jsonDataMap)
    {
        var enumValue = jsonDataMap.getEnum(super.key, enumClass);
        super.setValue(property, enumValue);
    }

}
