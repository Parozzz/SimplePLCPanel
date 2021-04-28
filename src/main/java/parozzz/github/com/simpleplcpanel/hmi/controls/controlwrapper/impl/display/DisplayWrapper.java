package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl.display;

import javafx.scene.control.Label;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;

public class DisplayWrapper extends LabeledWrapper<Label>
{
    public DisplayWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.DISPLAY, false);
    }

    @Override
    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<Label> attributeInitializer)
    {
        super.registerAttributeInitializers(attributeInitializer);

        attributeInitializer.addGlobals(AttributeType.READ_ADDRESS)
                .addStates(AttributeType.TEXT, AttributeType.VALUE)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();

                    TextAttribute textAttribute = null;
                    ValueAttribute valueAttribute = null;

                    for (var attribute : updateData.getAttributeList())
                    {
                        if (attribute instanceof TextAttribute)
                        {
                            textAttribute = (TextAttribute) attribute;
                        } else if (attribute instanceof ValueAttribute)
                        {
                            valueAttribute = (ValueAttribute) attribute;
                        }
                    }

                    if (textAttribute == null)
                    {
                        textAttribute = AttributeFetcher.fetch(this, AttributeType.TEXT);
                    }

                    if (valueAttribute == null)
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

}