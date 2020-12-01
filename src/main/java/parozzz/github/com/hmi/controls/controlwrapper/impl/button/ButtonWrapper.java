package parozzz.github.com.hmi.controls.controlwrapper.impl.button;

import javafx.scene.control.Button;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;

public final class ButtonWrapper
        extends LabeledWrapper<Button>
{
    public ButtonWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.BUTTON, ButtonWrapperValue::new);
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

                    for(var attributeType : updateData.getAttributeTypeList())
                    {
                        var attribute = AttributeFetcher.fetch(this, attributeType);
                        if (attribute == null)
                        {
                            continue;
                        }

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

    /*
    @Override
    protected void registerAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        super.registerAttributeInitializers(stateAttributeList, globalAttributeList);

        //GENERICS

        globalAttributeList.add(new ReadAddressAttribute(this));
        globalAttributeList.add(new ButtonDataAttribute(this));

        //STATE SPECIFIC
        stateAttributeList.add(new WriteAddressAttribute(this));
        stateAttributeList.add(new TextAttribute(this));
        stateAttributeList.add(new ValueAttribute(this));
    }

    @Override
    public void applyAttributes(Button control, Pane containerPane, AttributeMap attributeMap, Object involvedObject)
    {
        super.applyAttributes(control, containerPane, attributeMap, involvedObject);

        String text = "";

        var textAttribute = AttributeFetcher.fetch(attributeMap, TextAttribute.class);
        if (textAttribute != null)
        {
            control.setTextAlignment(textAttribute.getValue(TextAttribute.TEXT_ALIGNMENT));
            control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));

            text = textAttribute.getValue(TextAttribute.TEXT);
        }

        var valueAttribute = AttributeFetcher.fetch(attributeMap, ValueAttribute.class);
        if (valueAttribute != null)
        {
            super.setParsedTextPlaceholders(control, text, valueAttribute);
        }

    }*/

    @Override
    public void setup()
    {
        super.setup();
    }
}

        /*
        super.addAttributeInitializer(new TextAttribute(), TextAttribute.class, data ->
        {
            var control = data.getControl();
            var attribute = data.getAttribute();

            var text = attribute.getValue(TextAttribute.TEXT);
            var placeholderType = attribute.getValue(TextAttribute.TEXT_PLACEHOLDER_TYPE);
            super.setParsedTextPlaceholders(control, text, placeholderType);

            control.setTextAlignment(attribute.getValue(TextAttribute.TEXT_ALIGNMENT));
            control.setLineSpacing(attribute.getValue(TextAttribute.LINE_SPACING));
        });

        super.addAttributeInitializer(new ValueAttribute(), ValueAttribute.class, data ->
        {
            var control = data.getControl();
            var attribute = data.getAttribute();

        });*/
