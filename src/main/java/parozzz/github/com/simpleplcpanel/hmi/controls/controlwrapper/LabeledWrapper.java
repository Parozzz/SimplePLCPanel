package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper;

import javafx.scene.control.Labeled;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperStateChangedConsumer;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediateType;
import parozzz.github.com.simpleplcpanel.util.Validate;

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

        attributeInitializer.addGlobals(AttributeType.READ_ADDRESS)
                .addStates(AttributeType.TEXT, AttributeType.VALUE)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();

                    TextAttribute textAttribute = null;
                    ValueAttribute valueAttribute = null;
                    AddressAttribute readAddressAttribute = null;

                    for(var attribute : updateData.getAttributeList())
                    {
                        if (attribute instanceof FontAttribute)
                        {
                            control.setFont(((FontAttribute) attribute).getFont());
                            control.setUnderline(attribute.getValue(FontAttribute.UNDERLINE));
                            control.setAlignment(attribute.getValue(FontAttribute.TEXT_POSITION));
                            control.setTextFill(attribute.getValue(FontAttribute.TEXT_COLOR));
                        }
                        else if(attribute instanceof TextAttribute)
                        {
                            textAttribute = (TextAttribute) attribute;
                        }
                        else if(attribute instanceof ValueAttribute)
                        {
                            valueAttribute = (ValueAttribute) attribute;
                        }else if(attribute instanceof ReadAddressAttribute)
                        {
                            readAddressAttribute = (ReadAddressAttribute) attribute;

                            var readTag = readAddressAttribute.getValue(ReadAddressAttribute.READ_TAG);
                            if(readTag != null)
                            {
                                var readIntermediate = readTag.getReadIntermediate();
                                super.getStateMap().setState(readIntermediate.asInteger());
                            }
                        }
                    }

                    //If they are all false, it means none of my interest is being update and won't update.
                    if(textAttribute == null && valueAttribute == null && readAddressAttribute == null)
                    {
                        return;
                    }

                    if(textAttribute == null)
                    {
                        textAttribute = AttributeFetcher.fetch(this, AttributeType.TEXT);
                    }

                    if(valueAttribute == null)
                    {
                        valueAttribute = AttributeFetcher.fetch(this, AttributeType.VALUE);
                    }

                    if(readAddressAttribute == null)
                    {
                        readAddressAttribute = AttributeFetcher.fetch(this, AttributeType.READ_ADDRESS);
                    }

                    if (textAttribute != null && valueAttribute != null && readAddressAttribute != null)
                    {
                        control.setTextAlignment(textAttribute.getValue(TextAttribute.TEXT_ALIGNMENT));
                        control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));

                        var text = textAttribute.getValue(TextAttribute.TEXT);
                        this.setParsedTextPlaceholders(control, text, valueAttribute, readAddressAttribute);
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
            var readAddressAttribute = attributeMap.get(AttributeType.READ_ADDRESS);
            if (textAttribute != null && valueAttribute != null && readAddressAttribute != null)
            {
                var text = textAttribute.getValue(TextAttribute.TEXT);
                this.setParsedTextPlaceholders(super.control, text, valueAttribute, readAddressAttribute);
            }
        });
    }

    protected void setParsedTextPlaceholders(C control, String text,
            ValueAttribute valueAttribute, AddressAttribute readAddressAttribute)
    {
        if(!text.contains(ControlWrapper.VALUE_PLACEHOLDER))
        {
            control.setText(text);
            return;
        }

        var readTag = readAddressAttribute.getValue(ReadAddressAttribute.READ_TAG);
        var parseAs = valueAttribute.getValue(ValueAttribute.INTERMEDIATE_TYPE);
        var multiplyBy = valueAttribute.getValue(ValueAttribute.MULTIPLY_BY);
        var offset = valueAttribute.getValue(ValueAttribute.OFFSET);
        if(readTag == null || parseAs == null || multiplyBy == null || offset == null)
        {
            control.setText(text.replace(ControlWrapper.VALUE_PLACEHOLDER, ""));
            return;
        }

        var readIntermediate = readTag.getReadIntermediate();

        String stringPlaceholder;
        if (parseAs == ValueIntermediateType.BOOLEAN)
        {
            var languageSettings = super.getControlMainPage().getMainEditStage()
                    .getSettingsStage().getLanguage();
            stringPlaceholder = readIntermediate.asBoolean()
                    ? languageSettings.getONBooleanPlaceholder()
                    : languageSettings.getOFFBooleanPlaceholder();
        } else
        {
            var doubleValue = (readIntermediate.asDouble() + offset) * multiplyBy;
            stringPlaceholder = parseAs.convertNumber(doubleValue).toString();
        }

        control.setText(text.replace(ControlWrapper.VALUE_PLACEHOLDER, stringPlaceholder));
    }
}
