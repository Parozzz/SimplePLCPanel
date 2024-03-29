package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ControlWrapperAttributeUpdater<C extends Control> extends FXObject
{
    private final ControlWrapper<C> controlWrapper;
    private final C control;

    private final Set<ControlWrapperAttributeUpdateConsumer<C>> attributeUpdateConsumerSet;
    private final Set<ControlWrapperGenericAttributeUpdateConsumer> genericAttributeUpdateConsumerSet;

    public ControlWrapperAttributeUpdater(ControlWrapper<C> controlWrapper, C control)
    {
        this.controlWrapper = controlWrapper;
        this.control = control;

        this.attributeUpdateConsumerSet = new HashSet<>();
        this.genericAttributeUpdateConsumerSet = new HashSet<>();
    }

    public void initialize(ControlWrapperAttributeInitializer<C> attributeInitializer)
    {
        controlWrapper.getStateMap().currentWrapperStateProperty()
                .addListener((observable, oldValue, newValue) ->
                        //When the Current Wrapper State change only update state attributes!
                        this.update(
                                control, controlWrapper.getContainerPane(),
                                controlWrapper.getAttributeTypeManager().getStateTypeCollection()
                        )
                );

        attributeUpdateConsumerSet.addAll(attributeInitializer.attributeUpdateConsumerSet);
    }

    public void setAllAttributesTo(C control, Pane containerPane)
    {
        var attributeTypeManager = controlWrapper.getAttributeTypeManager();
        this.update(control, containerPane, attributeTypeManager.getTypeCollection());
    }

    public void updateAllAttributes()
    {
        this.setAllAttributesTo(control, controlWrapper.getContainerPane());
    }

    public void updateAttribute(AttributeType<?> attributeType)
    {
        var attributeTypeManager = controlWrapper.getAttributeTypeManager();
        if (attributeTypeManager.hasType(attributeType))
        {
            this.update(control, controlWrapper.getContainerPane(), Collections.singleton(attributeType));
        }
    }

    private void update(C control, Pane containerPane, Collection<AttributeType<?>> attributeTypeCollection)
    {
        var updateData = new ControlWrapperAttributeUpdateConsumer.UpdateData<>(
                controlWrapper, control, containerPane, attributeTypeCollection
        );
        attributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
        genericAttributeUpdateConsumerSet.forEach(updateConsumer -> updateConsumer.update(updateData));
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
