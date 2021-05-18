package parozzz.github.com.simpleplcpanel.hmi.attribute;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.util.Validate;

public abstract class Attribute extends FXObject// implements Cloneable
{
    protected final AttributeMap attributeMap;
    private final AttributePropertyManager attributePropertyManager;
    private final AttributeType<?> attributeType;

    public Attribute(AttributeMap attributeMap,
            AttributeType<?> attributeType, String name)
    {
        super(name);

        this.attributeMap = attributeMap;
        this.attributePropertyManager = new AttributePropertyManager(this);
        //This is here in case of human error
        Validate.needTrue("Invalid Attribute Type", attributeType.getAttributeClass().isAssignableFrom(this.getClass()));
        this.attributeType = attributeType;
    }

    public AttributeType<?> getType()
    {
        return attributeType;
    }

    public AttributeMap getAttributeMap()
    {
        return attributeMap;
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

    public abstract void update();

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
