package parozzz.github.com.hmi.attribute;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

public abstract class Attribute extends FXObject implements Cloneable
{
    private final AttributePropertyManager attributePropertyManager;

    public Attribute(String name)
    {
        super(name);

        this.attributePropertyManager = new AttributePropertyManager();
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

    public abstract void updateInternals();

    public abstract Attribute cloneEmpty();

    @Override
    public Attribute clone()
    {
        var clone = this.cloneEmpty();
        attributePropertyManager.forEachPropertyBis(propertyBis ->
        {
            //This should be fine since if they have the same attribute property, they have the same type!
            //Otherwise something is not working properly ... (And now a check is in place >:) )
            var attributeProperty = propertyBis.getAttributeProperty();
            Property property = propertyBis.getProperty();

            Property cloneProperty = clone.getAttributePropertyManager().getByAttributeProperty(attributeProperty);
            if(cloneProperty != null)
            {
                cloneProperty.setValue(property.getValue());
            }
        });

        return clone;
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

        this.updateInternals();
    }
}
