package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.beans.property.Property;
import javafx.scene.control.Control;
import parozzz.github.com.hmi.attribute.Attribute;

public final class SetupPaneControl<A extends Attribute, P>
{
    private final Control control;
    private final Property<P> property;
    public SetupPaneControl(Control control, Property<P> property)
    {
        this.control = control;
        this.property = property;
    }

    public Control getControl()
    {
        return control;
    }

    public Property<P> getProperty()
    {
        return property;
    }
}
