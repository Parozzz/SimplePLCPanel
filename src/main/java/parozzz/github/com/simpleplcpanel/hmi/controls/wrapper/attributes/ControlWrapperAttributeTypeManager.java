package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes;

import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;

import java.util.*;
import java.util.function.Consumer;

public final class ControlWrapperAttributeTypeManager extends FXObject
{
    private Set<AttributeType<?>> stateAttributeTypeSet;
    private Set<AttributeType<?>> globalAttributeTypeSet;
    private Set<AttributeType<?>> attributeTypeSet;

    public ControlWrapperAttributeTypeManager()
    {
    }

    public void initialize(ControlWrapperAttributeInitializer<?> attributeInitializer)
    {
        stateAttributeTypeSet = Set.copyOf(attributeInitializer.stateAttributeTypeList);
        globalAttributeTypeSet = Set.copyOf(attributeInitializer.globalAttributeTypeList);

        attributeTypeSet = new HashSet<>();
        attributeTypeSet.addAll(stateAttributeTypeSet);
        attributeTypeSet.addAll(globalAttributeTypeSet);
        attributeTypeSet = Collections.unmodifiableSet(attributeTypeSet);
    }

    public Collection<AttributeType<?>> getTypeCollection()
    {
        return attributeTypeSet;
    }

    public Collection<AttributeType<?>> getStateTypeCollection()
    {
        return stateAttributeTypeSet;
    }

    public Collection<AttributeType<?>> getGlobalTypeCollection()
    {
        return globalAttributeTypeSet;
    }

    public boolean isState(AttributeType<?> attributeType)
    {
        return stateAttributeTypeSet.contains(attributeType);
    }

    public boolean isGlobal(AttributeType<?> attributeType)
    {
        return globalAttributeTypeSet.contains(attributeType);
    }

    public boolean hasType(AttributeType<?> attributeType)
    {
        return attributeTypeSet.contains(attributeType);
    }

    public void forEachState(Consumer<AttributeType<?>> consumer)
    {
        stateAttributeTypeSet.forEach(consumer);
    }

    public void forEachGlobal(Consumer<AttributeType<?>> consumer)
    {
        globalAttributeTypeSet.forEach(consumer);
    }

    public void forEach(Consumer<AttributeType<?>> consumer)
    {
        forEachState(consumer);
        forEachGlobal(consumer);
    }
}
