package parozzz.github.com.hmi.controls.controlwrapper.impl.button;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.control.ButtonDataAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;

import java.util.List;

public final class ButtonWrapper
        extends LabeledWrapper<Button>
{
    public ButtonWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.BUTTON, ButtonWrapperValue::new);
    }

    @Override
    protected void setupAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        super.setupAttributeInitializers(stateAttributeList, globalAttributeList);

        //GENERICS
        globalAttributeList.add(new ReadAddressAttribute());
        globalAttributeList.add(new ButtonDataAttribute());

        //STATE SPECIFIC
        stateAttributeList.add(new WriteAddressAttribute());
        stateAttributeList.add(new TextAttribute());
        stateAttributeList.add(new ValueAttribute());
    }

    @Override
    public void applyAttributes(Button control, Pane containerPane, AttributeMap attributeMap)
    {
        super.applyAttributes(control, containerPane, attributeMap);

        String text = "";

        var textAttribute = AttributeFetcher.fetch(attributeMap, TextAttribute.class);
        if(textAttribute != null)
        {
            control.setTextAlignment(textAttribute.getValue(TextAttribute.TEXT_ALIGNMENT));
            control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));

            text = textAttribute.getValue(TextAttribute.TEXT);
        }

        var valueAttribute = AttributeFetcher.fetch(attributeMap, ValueAttribute.class);
        if(valueAttribute != null)
        {
            super.setParsedTextPlaceholders(control, text, valueAttribute);
        }

    }

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
