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

    public AttributeProperty(String key, T defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey()
    {
        return key;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public abstract Data<T> createData(Attribute attribute);
    /*
    {
        return new Data<>(this, new SimpleObjectProperty<>(defaultValue));
    }*/

    //public abstract void serializeInto(Property<T> property, JSONDataMap jsonDataMap);

    //public abstract void deserializeFrom(Property<T> property, JSONDataMap jsonDataMap);

    public abstract static class Data<P>
    {
        protected final AttributeProperty<P> attributeProperty;
        protected final Property<P> property;

        protected Data(AttributeProperty<P> attributeProperty)
        {
            this.attributeProperty = attributeProperty;
            this.property = new SimpleObjectProperty<>(attributeProperty.getDefaultValue());
        }

        public AttributeProperty<P> getAttributeProperty()
        {
            return attributeProperty;
        }

        public Property<P> getProperty()
        {
            return property;
        }

        protected void setValue(P value)
        {
            property.setValue(
                    Objects.requireNonNullElse(
                            value, attributeProperty.getDefaultValue()
                    )
            );
        }

        public abstract void serializeInto(JSONDataMap jsonDataMap);

        public abstract void deserializeFrom(JSONDataMap jsonDataMap);
    }
}
