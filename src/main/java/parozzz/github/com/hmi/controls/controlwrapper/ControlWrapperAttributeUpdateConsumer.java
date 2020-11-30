package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.Attribute;

import java.util.List;

@FunctionalInterface
public interface ControlWrapperAttributeUpdateConsumer<C extends Control>
{
    void update(UpdateData<C> updateData);

    class UpdateData<C>
    {
        private final C control;
        private final Pane containerPane;
        private final List<Class<? extends Attribute>> attributeClassList;

        public UpdateData(C control, Pane containerPane, Class<? extends Attribute>[] attributeClasses)
        {
            this.control = control;
            this.containerPane = containerPane;
            this.attributeClassList = List.of(attributeClasses);
        }

        public List<Class<? extends Attribute>> getAttributeClassList()
        {
            return attributeClassList;
        }

        public C getControl()
        {
            return control;
        }

        public Pane getContainerPane()
        {
            return containerPane;
        }
    }
}
