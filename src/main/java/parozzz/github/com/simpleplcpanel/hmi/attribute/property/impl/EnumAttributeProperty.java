package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
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
    public Data<E> createData(Attribute attribute)
    {
        return null;
    }
/*
    @Override
    public void serializeInto(Property<E> property, JSONDataMap jsonDataMap)
    {
        var value = property.getValue();
        if(value != null)
        {
            jsonDataMap.set(super.key, value);
        }
    }

    @Override
    public void deserializeFrom(Property<E> property, JSONDataMap jsonDataMap)
    {
        var enumValue = jsonDataMap.getEnum(super.key, enumClass);
        super.setValue(property, enumValue);
    }*/

    public class EnumData extends AttributeProperty.Data<E>
    {

        protected EnumData()
        {
            super(EnumAttributeProperty.this);
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
            var enumValue = jsonDataMap.getEnum(key, enumClass);
            super.setValue(enumValue);
        }
    }

}
