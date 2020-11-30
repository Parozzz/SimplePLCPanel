package parozzz.github.com.hmi.controls.controlwrapper.impl.display;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.LabeledWrapper;

import java.util.List;

public class DisplayWrapper extends LabeledWrapper<Label>
{
    public DisplayWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.DISPLAY, DisplayWrapperValue::new);
    }

    @Override
    protected void registerAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        super.registerAttributeInitializers(stateAttributeList, globalAttributeList);

        //GENERICS
        globalAttributeList.add(new ReadAddressAttribute(this));

        //STATE SPECIFIC
        stateAttributeList.add(new TextAttribute(this));
        stateAttributeList.add(new ValueAttribute(this));
    }

    @Override
    public void applyAttributes(Label control, Pane containerPane, AttributeMap attributeMap, Object involvedObject)
    {
        super.applyAttributes(control, containerPane, attributeMap, involvedObject);

        String text = "";

        var textAttribute = AttributeFetcher.fetch(attributeMap, TextAttribute.class);
        if(textAttribute != null)
        {
            text = textAttribute.getValue(TextAttribute.TEXT);
            /*var placeholderType = textAttribute.getValue(TextAttribute.TEXT_PLACEHOLDER_TYPE);
            super.setParsedTextPlaceholders(control, text, placeholderType);*/

            control.setTextAlignment(textAttribute.getValue(TextAttribute.TEXT_ALIGNMENT));
            control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));
        }

        var valueAttribute = AttributeFetcher.fetch(attributeMap, ValueAttribute.class);
        if(valueAttribute != null)
        {
            super.setParsedTextPlaceholders(control, text, valueAttribute);
        }
    }

}
