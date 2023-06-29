package parozzz.github.com.simpleplcpanel.hmi.controls.setup.attributechanger;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes.SetupPane;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.util.*;
import java.util.function.Function;

public final class SetupPaneAttributeChangerList<A extends Attribute>
        implements Iterable<SetupPaneAttributeChanger<A>>, Loggable
{
    private final SetupPane<A> setupPane;
    private final AttributeType<A> attributeType;
    private final Set<SetupPaneAttributeChanger<A>> attributeChangerSet;
    private final Map<Property<?>, SetupPaneAttributeChanger<A>> propertyToAttributeChangerMap;

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

    public SetupPaneAttributeChangerList<A> add(SetupPaneAttributeChanger<A> attributeChanger)
    {
        if(!attributeChangerSet.add(attributeChanger))
        {
            MainLogger.getInstance().warning("Trying to add a SetupPaneAttributeChanger twice", this);
            return this;
        }

        propertyToAttributeChangerMap.put(attributeChanger.getProperty(), attributeChanger);
        return this;
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
