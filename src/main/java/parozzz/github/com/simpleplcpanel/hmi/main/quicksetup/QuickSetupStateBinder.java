package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class QuickSetupStateBinder
{
    private final QuickSetupPane quickSetupPane;
    private final Set<BoundProperty<?, ?>> boundPropertySet;
    private final Map<AttributeType<?>, BoundAttributePropertySet<?>> attributeBoundPropertySetMap;

    private final List<Consumer<AttributeType<?>>> loadConsumerList;
    private final List<Consumer<AttributeType<?>>> writeConsumerList;

    private boolean ignoreAttributeUpdate;

    QuickSetupStateBinder(QuickSetupPane quickSetupPane)
    {
        this.quickSetupPane = quickSetupPane;
        this.boundPropertySet = new HashSet<>();
        this.attributeBoundPropertySetMap = new HashMap<>();

        this.loadConsumerList = new ArrayList<>();
        this.writeConsumerList = new ArrayList<>();
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
                //I need to use directly the attribute map to keep the type sealed inside the "BoundAttributeProperty"
                //Otherwise using a generic for the type of AttributeType will not work.
                boundAttributeProperty.copyFromAttributeMap(attributeMap);
            }
        }

        loadConsumerList.forEach(consumer -> consumer.accept(attributeType));
    }

    public void loadAllValuesFromControlWrapper()
    {
        var selectedControlWrapper = quickSetupPane.getSelectedControlWrapper();
        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.getAttributeTypeManager().forEach(this::loadValueFromControlWrapperOf);
        }
    }

    public <A extends Attribute> Builder<A> builder(AttributeType<A> attributeType)
    {
        return new Builder<>(attributeType);
    }

    public <T, A extends Attribute> void addDirectProperty(Property<T> property,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier,
            T defaultValue)
    {
        this.addIndirectProperty(property, Function.identity(), Function.identity(),
                attributeType, attributePropertySupplier, defaultValue);
    }

    public <T, H, A extends Attribute> void addIndirectProperty(Property<H> property,
            Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier,
            H defaultValue)
    {
        var boundProperty = new BoundProperty<>(property, attributePropertySupplier, attributeToQuick, defaultValue);
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
            if(attributeMap != null)
            {
                var attributeProperty = attributePropertySupplier.get();
                if(attributeProperty != null)
                {
                    var attribute = attributeMap.get(attributeType);
                    if(attribute != null)
                    {
                        if(newValue == null)
                        {
                            if(attributeProperty.allowNullValues())
                            {
                                attribute.setValue(attributeProperty, null);
                            }
                        }
                        else
                        {
                            var attributeNewValue = quickToAttribute.apply(newValue);
                            attribute.setValue(attributeProperty, attributeNewValue);
                        }
                    }
                }
            }

            writeConsumerList.forEach(consumer -> consumer.accept(attributeType));
        });
    }


    public <T, A extends Attribute> void addReadOnlyDirectProperty(Property<T> property,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier,
            T defaultValue)
    {
        this.addReadOnlyIndirectProperty(property, Function.identity(), attributeType,
                attributePropertySupplier, defaultValue);
    }

    public <T, H, A extends Attribute> void addReadOnlyIndirectProperty(Property<H> property,
            Function<T, H> attributeToQuick,
            AttributeType<A> attributeType, Supplier<AttributeProperty<T>> attributePropertySupplier,
            H defaultValue)
    {
        var boundProperty = new BoundProperty<>(property, attributePropertySupplier, attributeToQuick, defaultValue);
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
        private final V defaultValue;

        public BoundProperty(Property<V> property,
                Supplier<AttributeProperty<H>> attributePropertySupplier, Function<H, V> converter,
                V defaultValue)
        {
            this.property = property;
            this.attributePropertySupplier = attributePropertySupplier;
            this.converter = converter;
            this.defaultValue = defaultValue;
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
                if(attributeProperty.allowNullValues())
                {
                    property.setValue(null);
                }
                else if(defaultValue != null)
                {
                    property.setValue(defaultValue);
                }

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

        public Builder<A> addLoadConsumer(Consumer<AttributeType<?>> consumer)
        {
            QuickSetupStateBinder.this.loadConsumerList.add(consumer);
            return this;
        }

        public Builder<A> addWriteConsumer(Consumer<AttributeType<?>> consumer)
        {
            QuickSetupStateBinder.this.writeConsumerList.add(consumer);
            return this;
        }

        /*
            INDIRECT
        */
        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                Supplier<AttributeProperty<T>> attributePropertySupplier, H defaultValue)
        {
            QuickSetupStateBinder.this.addIndirectProperty(
                    property, quickToAttribute, attributeToQuick,
                    attributeType, attributePropertySupplier, defaultValue
            );
            return this;
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            return this.indirect(
                    property, quickToAttribute, attributeToQuick, attributePropertySupplier, null
            );
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                AttributeProperty<T> attributeProperty, H defaultValue)
        {
            return this.indirect(
                    property, quickToAttribute, attributeToQuick, () -> attributeProperty, defaultValue
            );
        }

        public <T, H> Builder<A> indirect(Property<H> property,
                Function<H, T> quickToAttribute, Function<T, H> attributeToQuick,
                AttributeProperty<T> attributeProperty)
        {
            return this.indirect(
                    property, quickToAttribute, attributeToQuick, () -> attributeProperty, null
            );
        }
        /*
            INDIRECT
        */
        /*
            READ ONLY INDIRECT
         */
        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, Supplier<AttributeProperty<T>> attributePropertySupplier,
                H defaultValue)
        {
            QuickSetupStateBinder.this.addReadOnlyIndirectProperty(
                    property, attributeToQuick, attributeType, attributePropertySupplier, defaultValue
            );
            return this;
        }

        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            return this.readOnlyIndirect(
                    property, attributeToQuick, attributePropertySupplier, null
            );
        }

        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, AttributeProperty<T> attributeProperty,
                H defaultValue)
        {
            return this.readOnlyIndirect(
                    property, attributeToQuick, attributeProperty, defaultValue
            );
        }

        public <T, H> Builder<A> readOnlyIndirect(Property<H> property,
                Function<T, H> attributeToQuick, AttributeProperty<T> attributeProperty)
        {
            return this.readOnlyIndirect(
                    property, attributeToQuick, attributeProperty, null
            );
        }
        /*
            READ ONLY INDIRECT
         */
        /*
            READ ONLY DIRECT
         */
        public <T> Builder<A> readOnlyDirect(Property<T> property, Supplier<AttributeProperty<T>> attributePropertySupplier,
                T defaultValue)
        {
            QuickSetupStateBinder.this.addReadOnlyDirectProperty(
                    property, attributeType, attributePropertySupplier, defaultValue
            );
            return this;
        }

        public <T> Builder<A> readOnlyDirect(Property<T> property, Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            return this.readOnlyDirect(property, attributePropertySupplier, null);
        }

        public <T> Builder<A> readOnlyDirect(Property<T> property, AttributeProperty<T> attributeProperty, T defaultValue)
        {
            return this.readOnlyDirect(property, () -> attributeProperty, defaultValue);
        }

        public <T> Builder<A> readOnlyDirect(Property<T> property, AttributeProperty<T> attributeProperty)
        {
            return this.readOnlyDirect(property, () -> attributeProperty, null);
        }
        /*
            READ ONLY DIRECT
         */
        /*
            DIRECT
         */
        public <T> Builder<A> direct(Property<T> property, Supplier<AttributeProperty<T>> attributePropertySupplier,
                T defaultValue)
        {
            QuickSetupStateBinder.this.addDirectProperty(
                    property, attributeType, attributePropertySupplier, defaultValue
            );
            return this;
        }

        public <T> Builder<A> direct(Property<T> property, Supplier<AttributeProperty<T>> attributePropertySupplier)
        {
            return this.direct(property, attributePropertySupplier, null);
        }

        public <T> Builder<A> direct(Property<T> property, AttributeProperty<T> attributeProperty, T defaultValue)
        {
            return this.direct(property, () -> attributeProperty, defaultValue);
        }

        public <T> Builder<A> direct(Property<T> property, AttributeProperty<T> attributeProperty)
        {
            return this.direct(property, () -> attributeProperty, null);
        }
        /*
            DIRECT
         */
    }

}
