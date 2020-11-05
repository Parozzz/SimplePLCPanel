package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.util.Validate;

public class AttributeInitializer<C extends Control, A extends Attribute>
{
    private final A attribute;

    public AttributeInitializer(A attribute)
    {
        this.attribute = attribute;
    }

    public void addToMap(AttributeMap attributeMap)
    {
        Validate.needFalse("Trying to add an attribute twice to the same state. Attribute: ", attribute.getFXObjectName(),
                attributeMap.hasAttribute(attribute.getClass()));
        attributeMap.addAttribute(attribute);
    }

    public A getAttribute()
    {
        return attribute;
    }

    public static class Data<C extends Control, A extends Attribute>
    {
        private final A attribute;
        private final C control;
        private final Pane containerPane;

        public Data(A attribute, C control, Pane containerPane)
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
