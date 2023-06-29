package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper;

import javafx.scene.control.Control;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

public abstract class ControlWrapperValue<C extends Control> extends FXObject
{
    protected final C control;
    public ControlWrapperValue(C control)
    {
        this.control = control;
    }

    public abstract ValueIntermediate getInternalValue();

    public abstract ValueIntermediate getOutsideValue();
}
