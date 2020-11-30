package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import parozzz.github.com.hmi.attribute.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ControlWrapperAttributeInitializer<C extends Control>
{
    private final ControlWrapper<C> controlWrapper;

    private final List<Attribute> stateAttributeList;
    private final List<Attribute> globalAttributeList;
    private final Map<Class<? extends Attribute>, ControlWrapperAttributeUpdateConsumer<?, C>> attributeSetConsumerMap;

    public ControlWrapperAttributeInitializer(ControlWrapper<C> controlWrapper)
    {
        this.controlWrapper = controlWrapper;

        this.stateAttributeList = new ArrayList<>();
        this.globalAttributeList = new ArrayList<>();

        this.attributeSetConsumerMap = new HashMap<>();
    }

    public <A extends Attribute> void addState(A attribute, ControlWrapperAttributeUpdateConsumer<A, C> setConsumer)
    {
        this.addState(attribute);
        attributeSetConsumerMap.put(attribute.getClass(), setConsumer);
    }

    public void addState(Attribute attribute)
    {
        stateAttributeList.add(attribute);
    }

    public <A extends Attribute> void addGlobal(A attribute, ControlWrapperAttributeUpdateConsumer<A, C> setConsumer)
    {
        this.addGlobal(attribute);
        attributeSetConsumerMap.put(attribute.getClass(), setConsumer);
    }

    public  void addGlobal(Attribute attribute)
    {
        globalAttributeList.add(attribute);
    }
}
