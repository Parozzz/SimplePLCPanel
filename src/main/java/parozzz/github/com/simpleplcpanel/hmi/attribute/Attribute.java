package parozzz.github.com.simpleplcpanel.hmi.attribute;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class Attribute extends FXObject// implements Cloneable
{
    protected final AttributeMap attributeMap;
    private final AttributeType<?> attributeType;

    private final AttributePropertyManager attributePropertyManager;
    private final Map<Object, Runnable> attributeUpdateRunnableMap;

    public Attribute(AttributeMap attributeMap,
            AttributeType<?> attributeType, String name)
    {
        super(name);

        //This is here in case of human error
        Validate.needTrue("Invalid Attribute Type", attributeType.getAttributeClass().isAssignableFrom(this.getClass()));
        this.attributeType = attributeType;
        this.attributeMap = attributeMap;

        this.attributePropertyManager = new AttributePropertyManager(this);
        this.attributeUpdateRunnableMap = new IdentityHashMap<>();
    }

    public AttributeType<?> getType()
    {
        return attributeType;
    }

    public AttributeMap getAttributeMap()
    {
        return attributeMap;
    }

    public void addAttributeUpdaterRunnable(Object key, Runnable attributeUpdateRunnable)
    {
        attributeUpdateRunnableMap.put(key, attributeUpdateRunnable);
    }

    public void removeAttributeUpdaterRunnable(Object key)
    {
        attributeUpdateRunnableMap.remove(key);
    }

    public <P> Property<P> getProperty(AttributeProperty<P> attributeProperty)
    {
        return attributePropertyManager.getByAttributeProperty(attributeProperty);
    }

    public <P> void setValue(AttributeProperty<P> attributeProperty, P value)
    {
        var property = this.getProperty(attributeProperty);
        if(property != null)
        {
            property.setValue(value);
        }
    }

    public <P> P getValue(AttributeProperty<P> attributeProperty)
    {
        var property = this.getProperty(attributeProperty);
        return property == null ? null : property.getValue();
    }

    protected AttributePropertyManager getAttributePropertyManager()
    {
        return attributePropertyManager;
    }

    public void update()
    {
        attributeUpdateRunnableMap.values().forEach(Runnable::run);
    }

    public void copyInto(Attribute pasteAttribute)
    {
        attributePropertyManager.forEachAttributePropertyData(data ->
        {
            //This should be fine since if they have the same attribute property, they have the same type!
            //Otherwise something is not working properly ... (And now a check is in place >:) )
            var attributeProperty = data.getAttributeProperty();
            Property property = data.getProperty();

            Property pasteProperty = pasteAttribute.getAttributePropertyManager().getByAttributeProperty(attributeProperty);
            if(pasteProperty != null)
            {
                pasteProperty.setValue(property.getValue());
            }
        });
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = super.serialize();
        attributePropertyManager.serializeInto(jsonDataMap);

        return jsonDataMap;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);
        attributePropertyManager.deserializeFrom(jsonDataMap);

        this.update();
    }
}
