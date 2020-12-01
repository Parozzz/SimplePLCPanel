package parozzz.github.com.hmi.controls.controlwrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.AttributeType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface ControlWrapperGenericAttributeUpdateConsumer
{
    void update(GenericUpdateData updateData);

    class GenericUpdateData
    {
        private final Control control;
        private final Pane containerPane;
        private final List<AttributeType<?>> attributeTypeList;

        public GenericUpdateData(Control control, Pane containerPane, AttributeType<?>... attributeTypes)
        {
            this(control, containerPane, Arrays.asList(attributeTypes));
        }

        public GenericUpdateData(Control control, Pane containerPane, Collection<AttributeType<?>> attributeTypeCollection)
        {
            this.control = control;
            this.containerPane = containerPane;
            this.attributeTypeList = List.copyOf(attributeTypeCollection);
        }

        public List<AttributeType<?>> getAttributeTypeList()
        {
            return attributeTypeList;
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
