package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;

import java.util.Arrays;
import java.util.Collection;

public interface ControlWrapperGenericAttributeUpdateConsumer
{
    void update(GenericUpdateData updateData);

    class GenericUpdateData
    {
        private final ControlWrapper<?> controlWrapper;
        private final Control control;
        private final Pane containerPane;
        private final Collection<AttributeType<?>> attributeTypeCollection;

        public GenericUpdateData(ControlWrapper<?> controlWrapper, Control control, Pane containerPane,
                AttributeType<?>... attributeTypes)
        {
            this(controlWrapper, control, containerPane, Arrays.asList(attributeTypes));
        }

        public GenericUpdateData(ControlWrapper<?> controlWrapper, Control control, Pane containerPane,
                Collection<AttributeType<?>> attributeTypeCollection)
        {
            this.controlWrapper = controlWrapper;
            this.control = control;
            this.containerPane = containerPane;
            this.attributeTypeCollection = attributeTypeCollection;

            //DO NOT CACHE DATA HERE! IT MIGHT CAUSE PROBLEMS DURING UPDATING!
        }

        public ControlWrapper<?> getControlWrapper()
        {
            return controlWrapper;
        }

        public Collection<AttributeType<?>> getAttributeTypeCollection()
        {
            return attributeTypeCollection;
        }

        public Control getControl()
        {
            return control;
        }

        public Pane getContainerPane()
        {
            return containerPane;
        }
    }

}
