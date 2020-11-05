package parozzz.github.com.hmi.controls.controlwrapper.impl.textinput;

import javafx.scene.control.TextField;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperValue;
import parozzz.github.com.hmi.util.valueintermediate.StringIntermediate;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediate;

public class InputWrapperValue extends ControlWrapperValue<TextField>
{
    private final ControlWrapper<TextField> controlWrapper;

    private final ValueIntermediate internalValue;
    private final ValueIntermediate externalValue;

    public InputWrapperValue(ControlWrapper<TextField> controlWrapper, TextField control)
    {
        super(control);

        this.controlWrapper = controlWrapper;

        internalValue = new StringIntermediate();
        externalValue = new StringIntermediate();
    }

    @Override
    public void setup()
    {
        super.setup();

        control.textProperty().addListener((observableValue, oldValue, newValue) -> internalValue.setString(newValue));
    }

    @Override
    public ValueIntermediate getInternalValue()
    {
        return internalValue;
    }

    @Override
    public ValueIntermediate getOutsideValue()
    {
        return externalValue;
    }
}
