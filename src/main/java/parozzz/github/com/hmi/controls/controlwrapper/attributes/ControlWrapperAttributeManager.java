package parozzz.github.com.hmi.controls.controlwrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class ControlWrapperAttributeManager<C extends Control> extends FXObject
{
    private final ControlWrapper<C> controlWrapper;
    private final C control;

    private final Set<AttributeType<?>> stateAttributeTypeSet;
    private final Set<AttributeType<?>> globalAttributeTypeSet;
    private final Set<ControlWrapperAttributeUpdateConsumer<C>> attributeUpdateConsumerSet;
    private final Set<ControlWrapperGenericAttributeUpdateConsumer> genericAttributeUpdateConsumerSet;

    public ControlWrapperAttributeManager(ControlWrapper<C> controlWrapper, C control)
    {
        super("ControlWrapperAttributeManager");

        this.controlWrapper = controlWrapper;
        this.control = control;

        this.stateAttributeTypeSet = new HashSet<>();
        this.globalAttributeTypeSet = new HashSet<>();
        this.attributeUpdateConsumerSet = new HashSet<>();
        this.genericAttributeUpdateConsumerSet = new HashSet<>();
    }

    public void forEachStateType(Consumer<AttributeType<?>> consumer)
    {
        stateAttributeTypeSet.forEach(consumer);
    }

    public void forEachGlobalType(Consumer<AttributeType<?>> consumer)
    {
        globalAttributeTypeSet.forEach(consumer);
    }

    public void initialize(ControlWrapperAttributeInitializer<C> attributeInitializer)
    {
        stateAttributeTypeSet.addAll(attributeInitializer.stateAttributeTypeList);
        globalAttributeTypeSet.addAll(attributeInitializer.globalAttributeTypeList);
        attributeUpdateConsumerSet.addAll(attributeInitializer.attributeUpdateConsumerSet);
    }

    public void setAllAttributesTo(C control, Pane containerPane)
    {
        var allAttributeList = new ArrayList<>(stateAttributeTypeSet);
        allAttributeList.addAll(globalAttributeTypeSet);
        var updateData = new ControlWrapperAttributeUpdateConsumer.UpdateData<>(
                control, containerPane, allAttributeList
        );
        attributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
        genericAttributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
    }

    public void updateAllAttributes()
    {
        this.setAllAttributesTo(control, controlWrapper.getContainerPane());
    }

    public void updateAttribute(AttributeType<?> attributeType)
    {
        if(this.hasType(attributeType))
        {
            var updateData = new ControlWrapperAttributeUpdateConsumer.UpdateData<>(
                    control, controlWrapper.getContainerPane(), attributeType
            );
            attributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
            genericAttributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
        }
    }

    public boolean hasStateType(AttributeType<?> attributeType)
    {
        return stateAttributeTypeSet.contains(attributeType);
    }

    public boolean hasGlobalType(AttributeType<?> attributeType)
    {
        return globalAttributeTypeSet.contains(attributeType);
    }

    public boolean hasType(AttributeType<?> attributeType)
    {
        return stateAttributeTypeSet.contains(attributeType) ||
                globalAttributeTypeSet.contains(attributeType);
    }

    public void addUpdateConsumer(ControlWrapperAttributeUpdateConsumer<C> updateConsumer)
    {
        attributeUpdateConsumerSet.add(updateConsumer);
    }

    public void removeUpdateConsumer(ControlWrapperAttributeUpdateConsumer<C> updateConsumer)
    {
        attributeUpdateConsumerSet.remove(updateConsumer);
    }

    public void addGenericUpdateConsumer(ControlWrapperGenericAttributeUpdateConsumer updateConsumer)
    {
        genericAttributeUpdateConsumerSet.add(updateConsumer);
    }

    public void removeGenericUpdateConsumer(ControlWrapperGenericAttributeUpdateConsumer updateConsumer)
    {
        genericAttributeUpdateConsumerSet.remove(updateConsumer);
    }
}
