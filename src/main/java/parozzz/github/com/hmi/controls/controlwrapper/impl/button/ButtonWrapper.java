package parozzz.github.com.hmi.controls.controlwrapper.impl.button;

import javafx.scene.control.Button;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;

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

        attributeInitializer.addGlobals(AttributeType.READ_ADDRESS, AttributeType.BUTTON_DATA)
                .addStates(AttributeType.WRITE_ADDRESS, AttributeType.TEXT, AttributeType.VALUE)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();

                    TextAttribute textAttribute = null;
                    ValueAttribute valueAttribute = null;

                    for(var attribute : updateData.getAttributeList())
                    {
                        if(attribute instanceof TextAttribute)
                        {
                            textAttribute = (TextAttribute) attribute;
                        }
                        else if(attribute instanceof ValueAttribute)
                        {
                            valueAttribute = (ValueAttribute) attribute;
                        }
                    }

                    if(textAttribute == null)
                    {
                        textAttribute = AttributeFetcher.fetch(this, AttributeType.TEXT);
                    }

                    if(valueAttribute == null)
                    {
                        valueAttribute = AttributeFetcher.fetch(this, AttributeType.VALUE);
                    }

                    if (textAttribute != null && valueAttribute != null)
                    {
                        control.setTextAlignment(textAttribute.getValue(TextAttribute.TEXT_ALIGNMENT));
                        control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));

                        var text = textAttribute.getValue(TextAttribute.TEXT);
                        super.setParsedTextPlaceholders(control, text, valueAttribute);
                    }
                });
    }

    @Override
    public void setup()
    {
        super.setup();
    }
}