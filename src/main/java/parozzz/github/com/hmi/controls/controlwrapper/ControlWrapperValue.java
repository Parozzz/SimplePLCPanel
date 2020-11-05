package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Control;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediate;

public abstract class ControlWrapperValue<C extends Control> extends FXObject
{
    protected final C control;
    public ControlWrapperValue(C control)
    {
        super("ValueSupplier");

        this.control = control;
    }

    public abstract ValueIntermediate getInternalValue();

    public abstract ValueIntermediate getOutsideValue();
}
