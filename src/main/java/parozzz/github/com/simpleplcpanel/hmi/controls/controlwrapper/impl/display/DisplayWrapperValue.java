package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl.display;

import javafx.scene.control.Label;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperValue;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.BooleanIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.MixedIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

public class DisplayWrapperValue extends ControlWrapperValue<Label>
{
    private final ValueIntermediate internalValue;
    private final ValueIntermediate externalValue;

    public DisplayWrapperValue(ControlWrapper<Label> controlWrapper, Label control)
    {
        super(control);

        this.internalValue = new BooleanIntermediate();
        this.externalValue = new MixedIntermediate();
    }

    @Override public ValueIntermediate getInternalValue()
    {
        return internalValue;
    }

    @Override public ValueIntermediate getOutsideValue()
    {
        return externalValue;
    }
}
