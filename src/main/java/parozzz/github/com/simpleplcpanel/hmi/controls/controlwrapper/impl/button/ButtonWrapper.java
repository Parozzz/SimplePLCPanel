package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl.button;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.Objects;

public final class ButtonWrapper
        extends LabeledWrapper<Button>
{
    public ButtonWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.BUTTON, false);
    }

    @Override
    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<Button> attributeInitializer)
    {
        super.registerAttributeInitializers(attributeInitializer);

        attributeInitializer.addGlobals(AttributeType.BUTTON_DATA)
                .addStates(AttributeType.WRITE_ADDRESS);
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

            var writeAttribute = AttributeFetcher.fetch(this, AttributeType.WRITE_ADDRESS);
            Objects.requireNonNull(writeAttribute, "ButtonWrapper must have a WriteAddress");

            var writeTag = writeAttribute.getValue(AddressAttribute.COMMUNICATION_TAG);
            if(writeTag == null)
            {
                return;
            }

            var buttonDataAttribute = AttributeFetcher.fetch(this, AttributeType.BUTTON_DATA);
            Objects.requireNonNull(buttonDataAttribute, "ButtonWrapper must have a ButtonDataAttribute");

            var writeIntermediate = writeTag.getWriteIntermediate();
            switch (buttonDataAttribute.getValue(ButtonDataAttribute.TYPE))
            {
                case NORMAL:
                    writeIntermediate.setBoolean(true);
                    break;
                case TOGGLE:
                    var readAttribute = AttributeFetcher.fetch(this, AttributeType.READ_ADDRESS);
                    Objects.requireNonNull(readAttribute, "ButtonWrapper must have a ReadAddress");

                    var readTag = readAttribute.getValue(AddressAttribute.COMMUNICATION_TAG);
                    if(readTag == null)
                    {
                        writeIntermediate.setBoolean(!writeIntermediate.asBoolean());
                    }
                    else
                    {
                        var readIntermediate = readTag.getReadIntermediate();
                        writeIntermediate.setBoolean(!readIntermediate.asBoolean());
                    }
                    break;
                case SET_TO_ON:
                    writeIntermediate.setBoolean(true, true);
                    break;
                case SET_TO_OFF:
                    writeIntermediate.setBoolean(false, true);
                    break;
            }

        });

        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent ->
        {
            if (mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            var writeAttribute = AttributeFetcher.fetch(this, AttributeType.WRITE_ADDRESS);
            Objects.requireNonNull(writeAttribute, "ButtonWrapper must have a WriteAddress");

            var writeTag = writeAttribute.getValue(AddressAttribute.COMMUNICATION_TAG);
            if(writeTag == null)
            {
                return;
            }

            var attribute = AttributeFetcher.fetch(this, AttributeType.BUTTON_DATA);
            Objects.requireNonNull(attribute, "ButtonWrapper must have a ButtonDataAttribute");

            var writeIntermediate = writeTag.getWriteIntermediate();
            if (attribute.getValue(ButtonDataAttribute.TYPE) == ButtonWrapperType.NORMAL)
            {
                writeIntermediate.setBoolean(false);
            }
        });
    }
}