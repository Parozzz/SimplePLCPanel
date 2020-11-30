package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.Attribute;

@FunctionalInterface
public interface ControlWrapperAttributeUpdateConsumer<A extends Attribute, C extends Control>
{
    void update(UpdateData<A, C> updateData);

    class UpdateData<A, C>
    {
        private final A attribute;
        private final C control;
        private final Pane containerPane;
        public UpdateData(A attribute, C control, Pane containerPane)
        {
            this.attribute = attribute;
            this.control = control;
            this.containerPane = containerPane;
        }

        public A getAttribute()
        {
            return attribute;
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
