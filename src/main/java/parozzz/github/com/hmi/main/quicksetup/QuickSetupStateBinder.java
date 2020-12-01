package parozzz.github.com.hmi.main.quicksetup;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class QuickSetupStateBinder
{
    private final QuickSetupVBox quickSetupVBox;
    private final Set<BoundProperty<?, ?>> boundPropertySet;
    private final Map<AttributeType<?>, BoundAttributePropertySet<?>> attributeBoundPropertySetMap;

    private WrapperState boundWrapperState;
    private boolean ignoreAttributeUpdate;

    QuickSetupStateBinder(QuickSetupVBox quickSetupVBox)
    {
        this.quickSetupVBox = quickSetupVBox;
        this.boundPropertySet = new HashSet<>();
        this.attributeBoundPropertySetMap = new HashMap<>();
    }

    public void setIgnoreAttributeUpdate(boolean ignoreAttributeUpdate)
    {
        this.ignoreAttributeUpdate = ignoreAttributeUpdate;
    }

    public void setBoundWrapperState(WrapperState wrapperState)
    {
        this.boundWrapperState = wrapperState;
        this.updateAll();
    }

    public void updateValueOf(AttributeType<?> attributeType)
    {
        if(boundWrapperState != null)
        {
            var boundAttributeProperty = attributeBoundPropertySetMap.get(attributeType);
            if(boundAttributeProperty != null)
            {
                boundAttributeProperty.copyFromWrapperState(boundWrapperState);
            }
        }

    }

    public void updateAll()
    {
        if(boundWrapperState != null)
        {
            attributeBoundPropertySetMap.values().forEach(boundAttributePropertySet ->
                    boundAttributePropertySet.copyFromWrapperState(boundWrapperState)
            );
        }
    }

    public <A extends Attribute> Builder<A> builder(AttributeType<A> attributeType)
    {
        return new Builder<>(attributeType);
    }

    public <T, A extends Attribute> void addDirectProperty(Property<T> property,
            AttributeType<A> attributeType, AttributeProperty<T> attributeProperty)
    {
        this.addIndirectProperty(property, Function.identity(), Function.identity(),
                attributeType, attributeProperty);
    }

    public <T, H, A extends Attribute> void addIndirectProperty(Property<H> property,
            Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, AttributeProperty<T> attributeProperty)
    {
        var boundProperty = new BoundProperty<>(property, attributeProperty, attributeToQuick);
        boundPropertySet.add(boundProperty);

        attributeBoundPropertySetMap.computeIfAbsent(attributeType, BoundAttributePropertySet::new)
                .add(boundProperty);

        property.addListener((observable, oldValue, newValue) ->
        {
            if(boundWrapperState == null || ignoreAttributeUpdate)
            {
                return;
            }

            var attribute = boundWrapperState.getAttributeMap().get(attributeType);
            if(attribute != null)
            {
                var attributeNewValue = quickToAttribute.apply(newValue);
                attribute.setValue(attributeProperty, attributeNewValue);
            }
        });

    }

    private static class BoundAttributePropertySet<A extends Attribute>
    {
        private final AttributeType<A> attributeType;
        private final Set<BoundProperty<?, ?>> boundPropertySet;

        public BoundAttributePropertySet(AttributeType<A> attributeType)
        {
            this.attributeType = attributeType;
            this.boundPropertySet = new HashSet<>();
        }

        public void add(BoundProperty<?, ?> boundProperty)
        {
            boundPropertySet.add(boundProperty);
        }

        public void copyFromWrapperState(WrapperState wrapperState)
        {
            var attribute = wrapperState.getAttributeMap().get(attributeType);
            if(attribute != null)
            {
                boundPropertySet.forEach(boundProperty -> boundProperty.copyFromAttribute(attribute));
            }
        }
    }

    private static class BoundProperty<V, H>
    {
        private final Property<V> property;
        private final AttributeProperty<H> attributeProperty;
        private final Function<H, V> converter;

        public BoundProperty(Property<V> property,
                AttributeProperty<H> attributeProperty, Function<H, V> converter)
        {
            this.property = property;
            this.attributeProperty = attributeProperty;
            this.converter = converter;
        }

        public void copyFromAttribute(Attribute attribute)
        {
            var attributeValue = attribute.getValue(attributeProperty);
            property.setValue(converter.apply(attributeValue));
        }
    }

    public class Builder<A extends Attribute>
    {
        private final AttributeType<A> attributeType;

        public Builder(AttributeType<A> attributeType)
        {
            this.attributeType = attributeType;
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addIndirectProperty(property, quickToAttribute, attributeToQuick,
                    attributeType, attributeProperty);
            return this;
        }

        public <T> Builder<A> direct(Property<T> property, AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addDirectProperty(property, attributeType, attributeProperty);
            return this;
        }
    }

}
