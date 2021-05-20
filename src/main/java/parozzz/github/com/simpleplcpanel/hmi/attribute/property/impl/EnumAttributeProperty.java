package parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public class EnumAttributeProperty<E extends Enum<E>> extends AttributeProperty<E>
{
    private final Class<E> enumClass;
    public EnumAttributeProperty(String key, E defaultValue, boolean allowNullValues)
    {
        super(key, defaultValue, allowNullValues);

        enumClass = defaultValue.getDeclaringClass();
    }

    @Override
    public Data createData(Attribute attribute)
    {
        return new EnumData();
    }

    public class EnumData extends AttributeProperty<E>.Data
    {
        protected EnumData()
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
            var enumValue = jsonDataMap.getEnum(key, enumClass);
            super.setValue(enumValue);
        }
    }

}
