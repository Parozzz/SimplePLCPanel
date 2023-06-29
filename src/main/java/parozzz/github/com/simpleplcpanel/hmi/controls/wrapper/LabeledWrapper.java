package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper;

import javafx.scene.control.Labeled;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeInitializer;
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

        attributeInitializer.addGlobals(AttributeType.READ_ADDRESS)
                .addStates(AttributeType.TEXT, AttributeType.VALUE)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();

                    boolean requireTextUpdate = false;
                    for(var attributeType : updateData.getAttributeTypeCollection())
                    {
                        if(attributeType == AttributeType.FONT)
                        {
                            var attribute = AttributeFetcher.fetch(this, attributeType);
                            if (attribute instanceof FontAttribute)
                            {
                                control.setFont(((FontAttribute) attribute).getFont());
                                control.setUnderline(attribute.getValue(FontAttribute.UNDERLINE));
                                control.setAlignment(attribute.getValue(FontAttribute.POSITION));
                                control.setTextFill(attribute.getValue(FontAttribute.COLOR));
                            }
                        }
                        else if(attributeType == AttributeType.READ_ADDRESS)
                        {
                            requireTextUpdate = true;

                            var attribute = AttributeFetcher.fetch(this, attributeType);
                            if (attribute instanceof ReadAddressAttribute)
                            {
                                //I know that changing the stat here will cause another attribute update,
                                //updating some attributes twice. ReadAddressAttribute is a GLOBAL attribute, so
                                //this should happen only if edge cases caused by the user and not impact a lot during runtime.
                                var readTag = attribute.getValue(ReadAddressAttribute.READ_TAG);
                                if(readTag != null)
                                {
                                    var readIntermediate = readTag.getReadIntermediate();
                                    super.getStateMap().setNumericState(readIntermediate.asInteger());
                                }
                            }
                        }
                        else if(attributeType == AttributeType.TEXT || attributeType == AttributeType.VALUE)
                        {
                            requireTextUpdate = true;
                        }
                    }

                    //DO NOT CACHE STUFF HERE since the attributes below could change during the looping above!
                    if(requireTextUpdate)
                    {
                        var textAttribute = AttributeFetcher.fetch(this, AttributeType.TEXT);
                        var valueAttribute = AttributeFetcher.fetch(this, AttributeType.VALUE);
                        var readAddressAttribute = AttributeFetcher.fetch(this, AttributeType.READ_ADDRESS);

                        if(textAttribute == null || valueAttribute == null || readAddressAttribute == null)
                        {
                            return;
                        }

                        control.setTextAlignment(textAttribute.getValue(TextAttribute.ALIGNMENT));
                        control.setLineSpacing(textAttribute.getValue(TextAttribute.LINE_SPACING));

                        var text = textAttribute.getValue(TextAttribute.TEXT);
                        this.setParsedTextPlaceholders(control, text, valueAttribute, readAddressAttribute);
                    }
                });
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        super.getStateMap().currentWrapperStateProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null)
            {
                return;
            }

            var attributeMap = newValue.getAttributeMap();

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
