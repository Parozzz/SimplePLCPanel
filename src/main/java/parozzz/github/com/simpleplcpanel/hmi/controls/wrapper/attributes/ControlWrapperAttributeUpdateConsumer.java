package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;

import java.util.Collection;

@FunctionalInterface
public interface ControlWrapperAttributeUpdateConsumer<C extends Control>
{
    void update(UpdateData<C> updateData);

    class UpdateData<C extends Control> extends ControlWrapperGenericAttributeUpdateConsumer.GenericUpdateData
    {
        public UpdateData(ControlWrapper<C> controlWrapper, C control, Pane containerPane,
                AttributeType<?>... attributeTypes)
        {
            super(controlWrapper, control, containerPane, attributeTypes);
        }

        public UpdateData(ControlWrapper<C> controlWrapper, C control, Pane containerPane,
                Collection<AttributeType<?>> attributeTypeCollection)
        {
            super(controlWrapper, control, containerPane, attributeTypeCollection);
        }

        @Override
        public ControlWrapper<C> getControlWrapper()
        {
            return (ControlWrapper<C>) super.getControlWrapper();
        }

        @Override
        public C getControl()
        {
            return (C) super.getControl();
        }
    }
}
