package parozzz.github.com.simpleplcpanel.hmi.attribute.property;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

import java.util.Objects;

public abstract class AttributeProperty<T>
{
    protected final String key;
    protected final T defaultValue;
    private final boolean allowNullValues;

    public AttributeProperty(String key, T defaultValue, boolean allowNullValues)
    {
        this.key = key;
        this.defaultValue = defaultValue;
        this.allowNullValues = allowNullValues;
    }

    public String getKey()
    {
        return key;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public boolean allowNullValues()
    {
        return allowNullValues;
    }

    public abstract Data createData(Attribute attribute);

    public abstract class Data
    {
        protected final Property<T> property;

        protected Data()
        {
            this.property = new SimpleObjectProperty<>(defaultValue);
        }

        public AttributeProperty<T> getAttributeProperty()
        {
            return AttributeProperty.this;
        }

        public Property<T> getProperty()
        {
            return property;
        }

        protected void setValue(T value)
        {
            if(value == null)
            {
                if(allowNullValues)
                {
                    property.setValue(null);
                }
                else if(defaultValue != null)
                {
                    property.setValue(defaultValue);
                }

                return;
            }

            property.setValue(value);
        }

        public abstract void serializeInto(JSONDataMap jsonDataMap);

        public abstract void deserializeFrom(JSONDataMap jsonDataMap);
    }
}
