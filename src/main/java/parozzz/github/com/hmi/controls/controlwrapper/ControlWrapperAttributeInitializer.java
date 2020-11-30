package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import parozzz.github.com.hmi.attribute.Attribute;

import java.util.*;

public final class ControlWrapperAttributeInitializer<C extends Control>
{
    private final ControlWrapper<C> controlWrapper;

    private final List<Attribute> stateAttributeList;
    private final List<Attribute> globalAttributeList;
    private final Set<ControlWrapperAttributeUpdateConsumer<C>> attributeUpdateConsumerSet;

    public ControlWrapperAttributeInitializer(ControlWrapper<C> controlWrapper)
    {
        this.controlWrapper = controlWrapper;

        this.stateAttributeList = new ArrayList<>();
        this.globalAttributeList = new ArrayList<>();

        this.attributeUpdateConsumerSet = new HashSet<>();
    }

    public ControlWrapperAttributeInitializer<C> addState(Attribute attribute)
    {
        stateAttributeList.add(attribute);
        return this;
    }

    public  ControlWrapperAttributeInitializer<C> addGlobal(Attribute attribute)
    {
        globalAttributeList.add(attribute);
        return this;
    }

    public ControlWrapperAttributeInitializer<C> addAttributeUpdateConsumer(ControlWrapperAttributeUpdateConsumer<C> attributeUpdateConsumer)
    {
        attributeUpdateConsumerSet.add(attributeUpdateConsumer);
        return this;
    }

    public List<Attribute> getStateAttributeList()
    {
        return stateAttributeList;
    }

    public List<Attribute> getGlobalAttributeList()
    {
        return globalAttributeList;
    }
}
