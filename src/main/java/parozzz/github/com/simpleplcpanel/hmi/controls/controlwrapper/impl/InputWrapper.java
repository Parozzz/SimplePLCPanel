package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl;

import com.sun.javafx.scene.control.skin.FXVK;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.control.InputDataAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.hmi.util.FXNodeUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;

import java.util.Objects;

public class InputWrapper extends ControlWrapper<TextField>
{
    public enum Type
    {
        INTEGER,
        REAL,
        STRING;
    }

    public InputWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.NUMERIC_INPUT, true);
    }

    @Override
    public void setup()
    {
        super.setup();

        control.textProperty().addListener((observableValue, oldValue, newValue) -> {
            var writeAttribute = AttributeFetcher.fetch(InputWrapper.this, AttributeType.WRITE_ADDRESS);
            Objects.requireNonNull(writeAttribute, "A InputWrapper has no WriteAddress?");

            var tag = writeAttribute.getValue(WriteAddressAttribute.WRITE_TAG);
            if(tag != null)
            {
                tag.getWriteIntermediate().setString(newValue);
            }
        });

        control.setCursor(null); //Remove the cursor that appears when hovering on it (It hides the cursor for resizing and is baaad)
    }

    @Override
    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<TextField> attributeInitializer)
    {
        super.registerAttributeInitializers(attributeInitializer);

        attributeInitializer.addGlobals(AttributeType.INPUT_DATA, AttributeType.WRITE_ADDRESS, AttributeType.FONT)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();

                    for(var attribute : updateData.getAttributeList())
                    {
                        if(attribute instanceof InputDataAttribute)
                        {
                            switch(attribute.getValue(InputDataAttribute.TYPE))
                            {
                                case INTEGER:
                                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.NUMERIC.ordinal());
                                    control.setTextFormatter(
                                            FXTextFormatterUtil.integerBuilder()
                                                    .max(attribute.getValue(InputDataAttribute.INTEGER_MAX_VALUE))
                                                    .min(attribute.getValue(InputDataAttribute.INTEGER_MIN_VALUE))
                                                    .getTextFormatter()
                                    );
                                    break;
                                case REAL:
                                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.NUMERIC.ordinal());
                                    control.setTextFormatter(
                                            FXTextFormatterUtil.doubleBuilder()
                                                    .maxDecimals(attribute.getValue(InputDataAttribute.REAL_MAX_DECIMALS))
                                                    .max(attribute.getValue(InputDataAttribute.REAL_MAX_VALUE))
                                                    .min(attribute.getValue(InputDataAttribute.REAL_MIN_VALUE))
                                                    .getTextFormatter()
                                    );
                                    break;
                                case STRING:
                                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.TEXT.ordinal());
                                    control.setTextFormatter(
                                            FXTextFormatterUtil.limited(attribute.getValue(InputDataAttribute.CHARACTER_LIMIT))
                                    );
                                    break;
                            }

                            control.setText(""); //When this changes, to avoid problems clear the text
                        }else if(attribute instanceof FontAttribute)
                        {
                            var skin = control.getSkin(); //Seems like setting the skin even if already exists causes some... problems
                            if(skin == null) //Set the skin before to be sure that it has it (It's required after)
                            {
                                control.setSkin(new TextFieldSkin(control));
                            }

                            control.setFont(((FontAttribute) attribute).getFont());
                            control.setAlignment(attribute.getValue(FontAttribute.TEXT_POSITION));

                            var text = (Text) control.lookup(".text");
                            if(text != null)
                            {
                                //Seems like unbinding stuff that is not meant to cause graphical glitches :(
                                text.setUnderline(attribute.getValue(FontAttribute.UNDERLINE));
                                text.fillProperty().bind(attribute.getProperty(FontAttribute.TEXT_COLOR));
                            }

                            var caretPath = FXNodeUtil.getCaret(control); //Set this after to have it revert after changing the text fill
                            if(caretPath != null)
                            {
                                caretPath.fillProperty().bind(new SimpleObjectProperty<>(Color.BLACK));
                            }
                        }
                    }
                });
    }

}