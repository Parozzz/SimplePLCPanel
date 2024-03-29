package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes;

import javafx.scene.control.Control;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;

import java.util.*;

public final class ControlWrapperAttributeInitializer<C extends Control>
{
    final List<AttributeType<?>> stateAttributeTypeList;
    final List<AttributeType<?>> globalAttributeTypeList;
    final Set<ControlWrapperAttributeUpdateConsumer<C>> attributeUpdateConsumerSet;

    public ControlWrapperAttributeInitializer()
    {
        this.stateAttributeTypeList = new ArrayList<>();
        this.globalAttributeTypeList = new ArrayList<>();

        this.attributeUpdateConsumerSet = new HashSet<>();
    }

    public ControlWrapperAttributeInitializer<C> addStates(AttributeType<?>... attributeTypes)
    {
        stateAttributeTypeList.addAll(Arrays.asList(attributeTypes));
        return this;
    }

    public  ControlWrapperAttributeInitializer<C> addGlobals(AttributeType<?>... attributeTypes)
    {
        globalAttributeTypeList.addAll(Arrays.asList(attributeTypes));
        return this;
    }

    public ControlWrapperAttributeInitializer<C> addAttributeUpdateConsumer(ControlWrapperAttributeUpdateConsumer<C> attributeUpdateConsumer)
    {
        attributeUpdateConsumerSet.add(attributeUpdateConsumer);
        return this;
    }
}
