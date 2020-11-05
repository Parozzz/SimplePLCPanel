package parozzz.github.com.hmi.attribute.property;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.util.Objects;

public abstract class AttributeProperty<T>
{
    protected final String key;
    protected final T defaultValue;
    //protected final Property<T> property;

    public AttributeProperty(String key, T defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
        //this.property = new SimpleObjectProperty<>(defaultValue);
    }

    public String getKey()
    {
        return key;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public T getValueFrom(AttributePropertyManager attributePropertyManager)
    {
        var property = attributePropertyManager.getByAttributeProperty(this);
        return property != null ? property.getValue() : null;
    }

    protected void setValue(Property<T> property, T value)
    {
        property.setValue(Objects.requireNonNullElse(value, defaultValue));
    }

    public abstract void serializeInto(Property<T> property, JSONDataMap jsonDataMap);

    public abstract void deserializeFrom(Property<T> property, JSONDataMap jsonDataMap);

    @FunctionalInterface
    public interface ChangeHandler<T>
    {
        void handle(T oldValue, T newValue);
    }
}
