package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.BaseAttribute;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
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

        stateAttributeList.add(new BaseAttribute());
    }

    @Override
    public void applyAttributes(C control, Pane containerPane, AttributeMap attributeMap)
    {
        super.applyAttributes(control, containerPane, attributeMap);

        var baseAttribute = AttributeFetcher.fetch(attributeMap, BaseAttribute.class);
        if (baseAttribute != null)
        {
            control.setUnderline(baseAttribute.getValue(BaseAttribute.UNDERLINE));

            if (baseAttribute.getValue(BaseAttribute.ADAPT))
            {
                containerPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                containerPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                containerPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            } else
            {
                var width = baseAttribute.getValue(BaseAttribute.WIDTH);
                var height = baseAttribute.getValue(BaseAttribute.HEIGHT);

                containerPane.setPrefSize(width, height);
                containerPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                containerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }

            //control.setShape(attribute.getShape());
            control.setAlignment(baseAttribute.getValue(BaseAttribute.TEXT_POSITION));
            control.setFont(baseAttribute.getFont());
            control.setTextFill(baseAttribute.getValue(BaseAttribute.TEXT_COLOR));
        }
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStateMap().addStateValueChangedConsumer(wrapperState ->
        {
            var textAttribute = AttributeFetcher.fetch(wrapperState, TextAttribute.class);
            var valueAttribute = AttributeFetcher.fetch(wrapperState, ValueAttribute.class);
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
