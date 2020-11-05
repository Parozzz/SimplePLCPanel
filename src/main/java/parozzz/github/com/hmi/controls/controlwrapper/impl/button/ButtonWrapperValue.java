package parozzz.github.com.hmi.controls.controlwrapper.impl.button;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperValue;
import parozzz.github.com.hmi.util.valueintermediate.BooleanIntermediate;
import parozzz.github.com.hmi.util.valueintermediate.MixedIntermediate;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediate;

import java.util.Objects;

public final class ButtonWrapperValue extends ControlWrapperValue<Button>
{
    private final ControlWrapper<Button> controlWrapper;

    private final ValueIntermediate internalValue;
    private final ValueIntermediate externalValue;

    public ButtonWrapperValue(ControlWrapper<Button> controlWrapper, Button button)
    {
        super(button);

        this.controlWrapper = controlWrapper;

        this.internalValue = new BooleanIntermediate();
        this.externalValue = new MixedIntermediate();
    }

    @Override
    public void setup()
    {
        super.setup();

        //These needs to be EventHandler because i might use event filter to consume events and these should not fire
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            if (mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            var attribute = AttributeFetcher.fetch(controlWrapper, ButtonDataAttribute.class);
            Objects.requireNonNull(attribute, "ButtonWrapper must have a ButtonDataAttribute");

            switch (attribute.getValue(ButtonDataAttribute.TYPE))
            {
                case NORMAL:
                    this.internalValue.setBoolean(true);
                    break;
                case TOGGLE:
                    var externalValue = this.externalValue.asBoolean();
                    //Toggle the external value not the internal ...
                    this.internalValue.setBoolean(!externalValue, true);
                    break;
                case SET_TO_ON:
                    this.internalValue.setBoolean(true, true);
                    break;
                case SET_TO_OFF:
                    this.internalValue.setBoolean(false, true);
                    break;
            }

        });

        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent ->
        {
            if (mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            var attribute = AttributeFetcher.fetch(controlWrapper, ButtonDataAttribute.class);
            Objects.requireNonNull(attribute, "ButtonWrapper must have a ButtonDataAttribute");
            if (attribute.getValue(ButtonDataAttribute.TYPE) == ButtonWrapperType.NORMAL)
            {
                this.internalValue.setBoolean(false);
            }
        });
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
