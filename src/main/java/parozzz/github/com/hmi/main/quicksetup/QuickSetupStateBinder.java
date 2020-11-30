package parozzz.github.com.hmi.main.quicksetup;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
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
    private final Map<Class<?>, AttributeBoundPropertySet<?>> attributeBoundPropertySetMap;

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
        this.refreshValues();
    }

    public void refreshValues()
    {
        if (boundWrapperState == null)
        {
            return;
        }

        attributeBoundPropertySetMap.values().forEach(attributeBoundPropertySet ->
                attributeBoundPropertySet.copyFromWrapperState(boundWrapperState)
        );
    }

    public <A extends Attribute> Builder<A> builder(Class<A> attributeClass)
    {
        return new Builder<>(attributeClass);
    }

    public <T, A extends Attribute> void addDirectProperty(Property<T> property, Class<A> attributeClass,
            AttributeProperty<T> attributeProperty)
    {
        this.addIndirectProperty(property, Function.identity(), Function.identity(),
                attributeClass, attributeProperty);
    }

    public <T, H, A extends Attribute> void addIndirectProperty(Property<H> property,
            Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
            Class<A> attributeClass, AttributeProperty<T> attributeProperty)
    {
        var boundProperty = new BoundProperty<>(property, attributeProperty, attributeToQuick);
        boundPropertySet.add(boundProperty);

        attributeBoundPropertySetMap.computeIfAbsent(attributeClass,
                t -> new AttributeBoundPropertySet<>((Class<A>) t)
        ).add(boundProperty);

        property.addListener((observable, oldValue, newValue) ->
        {
            if (boundWrapperState == null || ignoreAttributeUpdate)
            {
                return;
            }

            var attribute = AttributeFetcher.fetch(boundWrapperState, attributeClass);
            if (attribute != null)
            {
                var attributeNewValue = quickToAttribute.apply(newValue);
                attribute.setValue(attributeProperty, attributeNewValue);
                attribute.update(); //Update internals first to allow some attribute to have their values refreshed

                quickSetupVBox.updateSelectedWrapperAttributes();
            }
        });

    }

    private static class AttributeBoundPropertySet<A extends Attribute>
    {
        private final Class<A> attributeClass;
        private final Set<BoundProperty<?, ?>> boundPropertySet;

        public AttributeBoundPropertySet(Class<A> attributeClass)
        {
            this.attributeClass = attributeClass;
            this.boundPropertySet = new HashSet<>();
        }

        public void add(BoundProperty<?, ?> boundProperty)
        {
            boundPropertySet.add(boundProperty);
        }

        public void copyFromWrapperState(WrapperState wrapperState)
        {
            var attribute = AttributeFetcher.fetch(wrapperState, attributeClass);
            if (attribute != null)
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
        private final Class<A> attributeClass;

        public Builder(Class<A> attributeClass)
        {
            this.attributeClass = attributeClass;
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addIndirectProperty(property, quickToAttribute, attributeToQuick,
                    attributeClass, attributeProperty);
            return this;
        }

        public <T> Builder<A> direct(Property<T> property, AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addDirectProperty(property, attributeClass, attributeProperty);
            return this;
        }
    }

}
