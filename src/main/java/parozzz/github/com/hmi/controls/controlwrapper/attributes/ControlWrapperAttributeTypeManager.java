package parozzz.github.com.hmi.controls.controlwrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

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

    public void forEachState(Consumer<AttributeType<?>> consumer)
    {
        stateAttributeTypeSet.forEach(consumer);
    }

    public void forEachGlobal(Consumer<AttributeType<?>> consumer)
    {
        globalAttributeTypeSet.forEach(consumer);
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
        return stateAttributeTypeSet.contains(attributeType) ||
                globalAttributeTypeSet.contains(attributeType);
    }
}
