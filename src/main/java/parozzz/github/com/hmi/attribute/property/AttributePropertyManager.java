package parozzz.github.com.hmi.attribute.property;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.util.Validate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AttributePropertyManager
{
    private final Attribute attribute;

    private final Map<AttributeProperty<?>, Property<?>> attributePropertyMap;
    private final Map<String, AttributeProperty<?>> attributePropertyKeyMap;
    private final Set<PropertyBis<?>> propertyBisSet;

    public AttributePropertyManager(Attribute attribute)
    {
        this.attribute = attribute;

        this.attributePropertyMap = new HashMap<>();
        this.attributePropertyKeyMap = new HashMap<>();
        this.propertyBisSet = new HashSet<>();
    }

    public void addAll(AttributeProperty<?>... attributeProperties)
    {
        Stream.of(attributeProperties).forEach(this::add);
    }

    public <P> AttributePropertyManager add(AttributeProperty<P> attributeProperty)
    {
        var key = attributeProperty.getKey();
        Validate.needFalse("Trying to add an attribute with the same key twice. Key: ", key, attributePropertyKeyMap.containsKey(key));

        var property = new SimpleObjectProperty<>(attributeProperty.getDefaultValue());
        property.addListener((observable, oldValue, newValue) -> {
            attribute.update();

            var attributeType = attribute.getType();
            attribute.getAttributeMap().getControlWrapper().getAttributeUpdater().updateAttribute(attributeType);
        });

        attributePropertyMap.put(attributeProperty, property);

        attributePropertyKeyMap.put(key, attributeProperty);
        propertyBisSet.add(new PropertyBis<>(attributeProperty, property));
        return this;
    }

    public void removeAll(AttributeProperty<?>... attributeProperties)
    {
        Stream.of(attributeProperties).forEach(this::remove);
    }

    public void remove(AttributeProperty<?> attributeProperty)
    {
        var removed = attributePropertyKeyMap.remove(attributeProperty.getKey(), attributeProperty);
        if(removed)
        {
            attributePropertyMap.remove(attributeProperty);
            propertyBisSet.removeIf(propertyBis -> propertyBis.getAttributeProperty() == attributeProperty);
        }
    }

    @SuppressWarnings("unchecked")
    public <P> Property<P> getByAttributeProperty(AttributeProperty<P> attributeProperty)
    {
        var property = attributePropertyMap.get(attributeProperty);
        return property != null ? (Property<P>) property : null;
    }

    public AttributeProperty<?> getByKey(String key)
    {
        return attributePropertyKeyMap.get(key);
    }

    public void forEachPropertyBis(Consumer<PropertyBis<?>> consumer)
    {
        propertyBisSet.forEach(consumer);
    }

    public void serializeInto(JSONDataMap jsonDataMap)
    {
        propertyBisSet.forEach(propertyBis -> propertyBis.serializeInto(jsonDataMap));
    }

    public void deserializeFrom(JSONDataMap jsonDataMap)
    {
        propertyBisSet.forEach(propertyBis -> propertyBis.deserializeFrom(jsonDataMap));
    }

    public static final class PropertyBis<P>
    {
        private final AttributeProperty<P> attributeProperty;
        private final Property<P> property;

        private PropertyBis(AttributeProperty<P> attributeProperty, Property<P> property)
        {
            this.attributeProperty = attributeProperty;
            this.property = property;
        }

        public AttributeProperty<P> getAttributeProperty()
        {
            return attributeProperty;
        }

        public Property<P> getProperty()
        {
            return property;
        }

        public void serializeInto(JSONDataMap jsonDataMap)
        {
            attributeProperty.serializeInto(property, jsonDataMap);
        }

        public void deserializeFrom(JSONDataMap jsonDataMap)
        {
            attributeProperty.deserializeFrom(property, jsonDataMap);
        }
    }
}
