package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper;

import javafx.scene.control.Labeled;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperStateChangedConsumer;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediateType;

public abstract class LabeledWrapper<C extends Labeled> extends ControlWrapper<C>
{
    public LabeledWrapper(ControlContainerPane controlContainerPane,
            ControlWrapperType<C, ?> wrapperType, boolean stateless)
    {
        super(controlContainerPane, wrapperType, stateless);
    }

    @Override
    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<C> attributeInitializer)
    {
        super.registerAttributeInitializers(attributeInitializer);
        if (super.isStateless())
        {
            attributeInitializer.addGlobals(AttributeType.FONT);
        } else
        {
            attributeInitializer.addStates(AttributeType.FONT);
        }

        attributeInitializer.addAttributeUpdateConsumer(updateData ->
        {
            var control = updateData.getControl();

            for (var attribute : updateData.getAttributeList())
            {
                if (attribute instanceof FontAttribute)
                {
                    control.setFont(((FontAttribute) attribute).getFont());
                    control.setUnderline(attribute.getValue(FontAttribute.UNDERLINE));
                    control.setAlignment(attribute.getValue(FontAttribute.TEXT_POSITION));
                    control.setTextFill(attribute.getValue(FontAttribute.TEXT_COLOR));
                }
            }
        });
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStateMap().addStateValueChangedConsumer((stateMap, oldState, changeType) ->
        {
            if (changeType != WrapperStateChangedConsumer.ChangeType.STATE_CHANGED)
            {
                return;
            }

            var currentState = stateMap.getCurrentState();

            var attributeMap = currentState.getAttributeMap();

            var textAttribute = attributeMap.get(AttributeType.TEXT);
            var valueAttribute = attributeMap.get(AttributeType.VALUE);
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

        //Use external value here ... WTF i was thinking?
        String stringPlaceholder;
        if (parseAs == ValueIntermediateType.BOOLEAN)
        {
            var languageSettings = super.getControlMainPage().getMainEditStage()
                    .getSettingsStage().getLanguage();
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