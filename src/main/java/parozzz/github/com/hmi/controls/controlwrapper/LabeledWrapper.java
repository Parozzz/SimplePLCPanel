package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediateType;

import java.util.List;
import java.util.function.BiFunction;

public abstract class LabeledWrapper<C extends Labeled> extends ControlWrapper<C>
{
    public LabeledWrapper(ControlContainerPane controlContainerPane,
            ControlWrapperType<C, ?> wrapperType,
            BiFunction<ControlWrapper<C>, C, ControlWrapperValue<C>> valueSupplierCreator)
    {
        super(controlContainerPane, wrapperType, valueSupplierCreator);
    }

    @Override
    protected void setupAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        super.setupAttributeInitializers(stateAttributeList, globalAttributeList);

        stateAttributeList.add(new FontAttribute());
        stateAttributeList.add(new SizeAttribute());
    }

    @Override
    public void applyAttributes(C control, Pane containerPane, AttributeMap attributeMap, Object involvedObject)
    {
        super.applyAttributes(control, containerPane, attributeMap, involvedObject);

        var fontAttribute = AttributeFetcher.fetch(attributeMap, FontAttribute.class);
        if (fontAttribute != null)
        {
            control.setFont(fontAttribute.getFont());
            control.setUnderline(fontAttribute.getValue(FontAttribute.UNDERLINE));
            control.setAlignment(fontAttribute.getValue(FontAttribute.TEXT_POSITION));
            control.setTextFill(fontAttribute.getValue(FontAttribute.TEXT_COLOR));
        }
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStateMap().addStateValueChangedConsumer((stateMap, oldState, changeType) ->
        {
            var currentState = stateMap.getCurrentState();

            var textAttribute = AttributeFetcher.fetch(currentState, TextAttribute.class);
            var valueAttribute = AttributeFetcher.fetch(currentState, ValueAttribute.class);
            if (textAttribute != null && valueAttribute != null)
            {
                var text = textAttribute.getValue(TextAttribute.TEXT);
                this.setParsedTextPlaceholders(super.control, text, valueAttribute);
            }
        });
    }

    protected void setParsedTextPlaceholders(C control, String text, ValueAttribute valueAttribute)
    {
        var parseAs = valueAttribute.getValue(ValueAttribute.INTERMEDIATE_TYPE);
        var multiplyBy = valueAttribute.getValue(ValueAttribute.MULTIPLY_BY);
        var offset = valueAttribute.getValue(ValueAttribute.OFFSET);

        var languageSettings = super.getControlMainPage().getMainEditStage().getSettingsStage().getLanguage();

        //Use external value here ... WTF i was thinking?
        String stringPlaceholder;
        if (parseAs == ValueIntermediateType.BOOLEAN)
        {
            stringPlaceholder = super.getValue().getOutsideValue().asBoolean()
                    ? languageSettings.getONBooleanPlaceholder()
                    : languageSettings.getOFFBooleanPlaceholder();
        } else
        {
            var doubleValue = (super.getValue().getOutsideValue().asDouble() + offset) * multiplyBy;
            stringPlaceholder = parseAs.convertNumber(doubleValue).toString();
        }

        control.setText(text.replace(ControlWrapper.VALUE_PLACEHOLDER, stringPlaceholder));
    }
}
