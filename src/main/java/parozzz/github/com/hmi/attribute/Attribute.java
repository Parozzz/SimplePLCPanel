package parozzz.github.com.hmi.attribute;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.util.function.Function;

public abstract class Attribute extends FXObject// implements Cloneable
{
    protected final ControlWrapper<?> controlWrapper;
    private final AttributePropertyManager attributePropertyManager;
    private final Function<ControlWrapper<?>, ? extends Attribute> creatorFunction;

    public Attribute(ControlWrapper<?> controlWrapper, String name,
            Function<ControlWrapper<?>, ? extends Attribute> creatorFunction)
    {
        super(name);

        this.controlWrapper = controlWrapper;
        this.attributePropertyManager = new AttributePropertyManager(this);
        this.creatorFunction = creatorFunction;
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

    public final Attribute cloneAsDefault(ControlWrapper<?> controlWrapper)
    {
        return creatorFunction.apply(controlWrapper);
    }

    public Attribute cloneAsDefaultWithSameControlWrapper()
    {
        return cloneAsDefault(this.controlWrapper);
    }

    public Attribute clone(ControlWrapper<?> controlWrapper)
    {
        var copy = this.cloneAsDefault(controlWrapper);
        this.copyInto(copy);
        return copy;
    }

    public void copyInto(Attribute pasteAttribute)
    {
        attributePropertyManager.forEachPropertyBis(propertyBis ->
        {
            //This should be fine since if they have the same attribute property, they have the same type!
            //Otherwise something is not working properly ... (And now a check is in place >:) )
            var attributeProperty = propertyBis.getAttributeProperty();
            Property property = propertyBis.getProperty();

            Property pasteProperty = pasteAttribute.getAttributePropertyManager().getByAttributeProperty(attributeProperty);
            if(pasteProperty != null)
            {
                pasteProperty.setValue(property.getValue());
            }
        });
    }
    /*
    public Attribute clone(ControlWrapper<?> controlWrapper)
    {
        var clone = this.createEmpty(controlWrapper);
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
*/
    /*
    public Attribute clone()
    {

        va clone = this.cloneEmpty();
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
    }*/

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
