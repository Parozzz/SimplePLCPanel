package parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.logger.Loggable;
import parozzz.github.com.logger.MainLogger;
import parozzz.github.com.util.Validate;

import java.util.*;
import java.util.function.Function;

public final class SetupPaneAttributeChangerList<A extends Attribute>
        implements Iterable<SetupPaneAttributeChanger<A>>, Loggable
{
    private final SetupPane<A> setupPane;
    private final AttributeType<A> attributeType;
    private final Set<SetupPaneAttributeChanger<A>> attributeChangerSet;
    private final Map<Property<?>, SetupPaneAttributeChanger<A>> propertyToAttributeChangerMap;

    private boolean ignoreCopy;

    public SetupPaneAttributeChangerList(SetupPane<A> setupPane, AttributeType<A> attributeType)
    {
        this.setupPane = setupPane;
        this.attributeType = attributeType;

        this.attributeChangerSet = new HashSet<>();
        this.propertyToAttributeChangerMap = new HashMap<>();
    }

    public AttributeType<A> getAttributeType()
    {
        return attributeType;
    }

    public Set<Property<?>> getPropertySet()
    {
        return Set.copyOf(propertyToAttributeChangerMap.keySet());
    }

    public SetupPaneAttributeChanger<A> getByProperty(Property<?> property)
    {
        return propertyToAttributeChangerMap.get(property);
    }

    public void resetAllDataChanged()
    {
        attributeChangerSet.forEach(SetupPaneAttributeChanger::resetDataChanged);
    }

    public SetupPaneAttributeChangerList<A> add(SetupPaneAttributeChanger<A> attributeChanger)
    {
        if(!attributeChangerSet.add(attributeChanger))
        {
            MainLogger.getInstance().warning("Trying to add a SetupPaneAttributeChanger twice", this);
        }

        propertyToAttributeChangerMap.put(attributeChanger.getProperty(), attributeChanger);
        return this;
    }

    //This should be a pretty hot method, better be safe with a reliable for loop than a stream
    public boolean isAnyDataChanged()
    {
        for(var attributeChanger : attributeChangerSet)
        {
            if(attributeChanger.isDataChanged())
            {
                return true;
            }
        }

        return false;
    }

    public void setDataToAttribute(AttributeMap attributeMap, boolean forceSet)
    {
        var attribute = attributeMap.get(attributeType);
        if(attribute == null)
        {
            return;
        }

        for(var attributeChanger : attributeChangerSet)
        {
            if(attributeChanger.isDataChanged() || forceSet)
            {
                ignoreCopy = true;
                attributeChanger.setDataToAttribute(attribute);
                ignoreCopy = false;
            }
        }
    }

    public void copyDataFromAttribute(AttributeMap attributeMap)
    {
        if(ignoreCopy)
        {
            return;
        }

        var attribute = attributeMap.get(attributeType);
        if(attribute == null)
        {
            return;
        }

        //I am copying the list because attribute could change it while iterating here
        for(var attributeChanger : attributeChangerSet)
        {
            attributeChanger.copyDataFromAttribute(attribute);
        }

        setupPane.doDataCopiedFromAttribute();
    }

    public <N extends Number> SetupPaneAttributeChangerList<A> createStringToNumber(Property<String> property,
            AttributeProperty<N> attributeProperty,
            Function<String, N> attributeToPropertyConvert)
    {
        return this.add(new SetupPaneAttributeChanger<>(setupPane,
                property, attributeProperty,
                attributeToPropertyConvert, Number::toString));
    }

    public <V, H> SetupPaneAttributeChangerList<A> create(Property<H> property,
            AttributeProperty<V> attributeProperty,
            Function<V, H> propertyToAttributeConvert,
            Function<H, V> attributeToPropertyConvert)
    {
        return this.add(new SetupPaneAttributeChanger<>(setupPane,
                property, attributeProperty,
                attributeToPropertyConvert, propertyToAttributeConvert));
    }

    public <V> SetupPaneAttributeChangerList<A> create(Property<V> property,
            AttributeProperty<V> attributeProperty)
    {
        return this.add(new SetupPaneAttributeChanger<>(setupPane,
                property, attributeProperty));
    }

    @Override
    public Iterator<SetupPaneAttributeChanger<A>> iterator()
    {
        return attributeChangerSet.iterator();
    }

    @Override
    public String log()
    {
        return "SetupPane: " + setupPane.getFXObjectName() +
                "AttributeType: " + attributeType;
    }
}
