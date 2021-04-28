package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ControlWrapperGenericAttributeUpdateConsumer
{
    void update(GenericUpdateData updateData);

    class GenericUpdateData
    {
        private final ControlWrapper<?> controlWrapper;
        private final Control control;
        private final Pane containerPane;
        private final List<Attribute> attributeList;

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

            attributeList = attributeTypeCollection.stream()
                    .map(attributeType -> AttributeFetcher.fetch(controlWrapper, attributeType))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableList());
        }

        public ControlWrapper<?> getControlWrapper()
        {
            return controlWrapper;
        }

        public List<Attribute> getAttributeList()
        {
            return attributeList;
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
