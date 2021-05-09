package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class QuickSetupStateBinder
{
    private final QuickSetupPane quickSetupPane;
    private final Set<BoundProperty<?, ?>> boundPropertySet;
    private final Map<AttributeType<?>, BoundAttributePropertySet<?>> attributeBoundPropertySetMap;

    private boolean ignoreAttributeUpdate;

    QuickSetupStateBinder(QuickSetupPane quickSetupPane)
    {
        this.quickSetupPane = quickSetupPane;
        this.boundPropertySet = new HashSet<>();
        this.attributeBoundPropertySetMap = new HashMap<>();
    }

    public void setIgnoreAttributeUpdate(boolean ignoreAttributeUpdate)
    {
        this.ignoreAttributeUpdate = ignoreAttributeUpdate;
    }

    public void loadValueFromControlWrapperOf(AttributeType<?> attributeType)
    {
        AttributeMap attributeMap = this.getAttributeMapOf(attributeType);
        if(attributeMap != null)
        {
            var boundAttributeProperty = attributeBoundPropertySetMap.get(attributeType);
            if(boundAttributeProperty != null)
            {
                boundAttributeProperty.copyFromAttributeMap(attributeMap);
            }
        }
    }

    public void loadAllValuesFromControlWrapper()
    {
        attributeBoundPropertySetMap.values().forEach(boundAttributePropertySet ->
        {
            var attributeMap = this.getAttributeMapOf(boundAttributePropertySet.attributeType);
            if(attributeMap != null)
            {
                boundAttributePropertySet.copyFromAttributeMap(attributeMap);
            }
        });
    }

    public <A extends Attribute> Builder<A> builder(AttributeType<A> attributeType)
    {
        return new Builder<>(attributeType);
    }

    public <T, A extends Attribute> void addDirectProperty(Property<T> property,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier)
    {
        this.addIndirectProperty(property, Function.identity(), Function.identity(),
                attributeType, attributePropertySupplier);
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
        this.addIndirectProperty(property, quickToAttribute, attributeToQuick, attributeType, () -> attributeProperty);
    }

    public <T, H, A extends Attribute> void addIndirectProperty(Property<H> property,
            Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier)
    {
        var boundProperty = new BoundProperty<>(property, attributePropertySupplier, attributeToQuick);
        boundPropertySet.add(boundProperty);

        attributeBoundPropertySetMap.computeIfAbsent(attributeType, BoundAttributePropertySet::new)
                .add(boundProperty);

        property.addListener((observable, oldValue, newValue) ->
        {
            if(ignoreAttributeUpdate)
            {
                return;
            }

            var attributeMap = this.getAttributeMapOf(attributeType);
            if(attributeMap == null)
            {
                return;
            }

            var attributeProperty = attributePropertySupplier.get();
            if(attributeProperty == null)
            {
                return;
            }

            var attribute = attributeMap.get(attributeType);
            if(attribute != null)
            {
                var attributeNewValue = quickToAttribute.apply(newValue);
                attribute.setValue(attributeProperty, attributeNewValue);
            }
        });
    }

    public <T, H, A extends Attribute> void addReadOnlyIndirectProperty(Property<H> property,
            Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, AttributeProperty<T> attributeProperty)
    {
        this.addReadOnlyIndirectProperty(property, attributeToQuick, attributeType, attributeProperty);
    }

    public <T, H, A extends Attribute> void addReadOnlyIndirectProperty(Property<H> property,
            Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier)
    {
        var boundProperty = new BoundProperty<>(property, attributePropertySupplier, attributeToQuick);
        boundPropertySet.add(boundProperty);

        attributeBoundPropertySetMap.computeIfAbsent(attributeType, BoundAttributePropertySet::new)
                .add(boundProperty);
    }

    private AttributeMap getAttributeMapOf(AttributeType<?> attributeType)
    {
        var selectedControlWrapper = quickSetupPane.getSelectedControlWrapper();
        if(selectedControlWrapper == null)
        {
            return null;
        }

        AttributeMap attributeMap = null;

        var attributeManager = selectedControlWrapper.getAttributeTypeManager();
        if(attributeManager.isState(attributeType))
        {
            var wrapperState = quickSetupPane.getSelectedWrapperState();
            if(wrapperState != null)
            {
                attributeMap = wrapperState.getAttributeMap();
            }
        }
        else if(attributeManager.isGlobal(attributeType))
        {
            attributeMap = selectedControlWrapper.getGlobalAttributeMap();
        }

        return attributeMap;
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

        public void copyFromAttributeMap(AttributeMap attributeMap)
        {
            var attribute = attributeMap.get(attributeType);
            if(attribute != null)
            {
                boundPropertySet.forEach(boundProperty -> boundProperty.copyFromAttribute(attribute));
            }
        }
    }

    private static class BoundProperty<V, H>
    {
        private final Property<V> property;
        private final Supplier<AttributeProperty<H>> attributePropertySupplier;
        private final Function<H, V> converter;

        public BoundProperty(Property<V> property,
                Supplier<AttributeProperty<H>> attributePropertySupplier, Function<H, V> converter)
        {
            this.property = property;
            this.attributePropertySupplier = attributePropertySupplier;
            this.converter = converter;
        }

        public void copyFromAttribute(Attribute attribute)
        {
            var attributeProperty = attributePropertySupplier.get();
            if(attribute == null)
            {
                return;
            }

            var attributeValue = attribute.getValue(attributeProperty);
            if(attributeValue == null)
            {
                return;
            }

            var converterValue = converter.apply(attributeValue);
            if(converterValue != null)
            {
                property.setValue(converterValue);
            }
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
                Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            QuickSetupStateBinder.this.addIndirectProperty(property, quickToAttribute, attributeToQuick,
                    attributeType, attributePropertySupplier);
            return this;
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addIndirectProperty(property, quickToAttribute, attributeToQuick,
                    attributeType, attributeProperty);
            return this;
        }

        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            QuickSetupStateBinder.this.addReadOnlyIndirectProperty(property, attributeToQuick, attributeType, attributePropertySupplier);
            return this;
        }

        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addReadOnlyIndirectProperty(property, attributeToQuick, attributeType, attributeProperty);
            return this;
        }

        public <T> Builder<A> direct(Property<T> property, Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            QuickSetupStateBinder.this.addDirectProperty(property, attributeType, attributePropertySupplier);
            return this;
        }

        public <T> Builder<A> direct(Property<T> property, AttributeProperty<T> attributeProperty)
        {
            QuickSetupStateBinder.this.addDirectProperty(property, attributeType, attributeProperty);
            return this;
        }
    }

}
