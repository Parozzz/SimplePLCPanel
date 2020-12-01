package parozzz.github.com.hmi.controls.controlwrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.AttributeType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface ControlWrapperAttributeUpdateConsumer<C extends Control>
{
    void update(UpdateData<C> updateData);

    class UpdateData<C extends Control> extends ControlWrapperGenericAttributeUpdateConsumer.GenericUpdateData
    {
        public UpdateData(C control, Pane containerPane, AttributeType<?>... attributeTypes)
        {
            super(control, containerPane, attributeTypes);
        }

        public UpdateData(C control, Pane containerPane, Collection<AttributeType<?>> attributeTypeCollection)
        {
            super(control, containerPane, attributeTypeCollection);
        }

        @Override
        public C getControl()
        {
            return (C) super.getControl();
        }
    }
}
