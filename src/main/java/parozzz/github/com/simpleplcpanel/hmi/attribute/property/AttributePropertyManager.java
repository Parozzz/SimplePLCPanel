package parozzz.github.com.simpleplcpanel.hmi.attribute.property;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.util.Validate;

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
    private final Set<AttributeProperty.Data<?>> attributePropertyDataSet;

    public AttributePropertyManager(Attribute attribute)
    {
        this.attribute = attribute;

        this.attributePropertyMap = new HashMap<>();
        this.attributePropertyKeyMap = new HashMap<>();
        this.attributePropertyDataSet = new HashSet<>();
    }

    public void addAll(AttributeProperty<?>... attributeProperties)
    {
        Stream.of(attributeProperties).forEach(this::add);
    }

    public <P> AttributePropertyManager add(AttributeProperty<P> attributeProperty)
    {
        var key = attributeProperty.getKey();

        var putReturn = attributePropertyKeyMap.putIfAbsent(key, attributeProperty);
        Validate.needTrue("Trying to add an attribute with the same key twice. Key: ",
                key, putReturn != attributeProperty);

        var attributePropertyData = attributeProperty.createData(attribute);
        attributePropertyDataSet.add(attributePropertyData);

        var property = attributePropertyData.getProperty();
        attributePropertyMap.put(attributeProperty, property);
        property.addListener((observable, oldValue, newValue) -> {
            attribute.update();

            var attributeType = attribute.getType();
            attribute.getAttributeMap().getControlWrapper()
                    .getAttributeUpdater().updateAttribute(attributeType);
        });
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
            attributePropertyDataSet.removeIf(propertyBis -> propertyBis.getAttributeProperty() == attributeProperty);
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

    public void forEachAttributePropertyData(Consumer<AttributeProperty.Data<?>> consumer)
    {
        attributePropertyDataSet.forEach(consumer);
    }

    public void serializeInto(JSONDataMap jsonDataMap)
    {
        attributePropertyDataSet.forEach(data ->
                data.serializeInto(jsonDataMap)
        );
    }

    public void deserializeFrom(JSONDataMap jsonDataMap)
    {
        attributePropertyDataSet.forEach(data ->
                data.deserializeFrom(jsonDataMap)
        );
    }
}
