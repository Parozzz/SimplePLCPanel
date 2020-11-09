package parozzz.github.com.hmi.controls.controlwrapper.impl.textinput;

import com.sun.javafx.scene.control.skin.FXVK;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.BaseAttribute;
import parozzz.github.com.hmi.attribute.impl.TextAttribute;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.control.InputDataAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.util.FXNodeUtil;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;

import java.util.List;

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
        super(controlContainerPane, ControlWrapperType.NUMERIC_INPUT, InputWrapperValue::new);
    }

    @Override
    public void setup()
    {
        super.setup();
        control.setCursor(null); //Remove the cursor that appears when hovering on it (It hides the cursor for resizing and is baaad)
    }

    @Override
    protected void setupAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        super.setupAttributeInitializers(stateAttributeList, globalAttributeList);

        //GENERICS
        globalAttributeList.add(new InputDataAttribute());

        //STATE SPECIFIC
        stateAttributeList.add(new WriteAddressAttribute());
        stateAttributeList.add(new BaseAttribute());
    }

    @Override
    public void applyAttributes(TextField control, Pane containerPane, AttributeMap attributeMap)
    {
        super.applyAttributes(control, containerPane, attributeMap);

        var inputDataAttribute = AttributeFetcher.fetch(this.getGlobalAttributeMap(), InputDataAttribute.class);
        if(inputDataAttribute != null)
        {
            var type = inputDataAttribute.getValue(InputDataAttribute.TYPE);
            switch (type)
            {
                case INTEGER:
                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.NUMERIC.ordinal());
                    control.setTextFormatter(
                            FXTextFormatterUtil.integerBuilder()
                                    .max(inputDataAttribute.getValue(InputDataAttribute.INTEGER_MAX_VALUE))
                                    .min(inputDataAttribute.getValue(InputDataAttribute.INTEGER_MIN_VALUE))
                                    .getTextFormatter()
                    );
                    break;
                case REAL:
                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.NUMERIC.ordinal());
                    control.setTextFormatter(
                            FXTextFormatterUtil.doubleBuilder()
                                    .maxDecimals(inputDataAttribute.getValue(InputDataAttribute.REAL_MAX_DECIMALS))
                                    .max(inputDataAttribute.getValue(InputDataAttribute.REAL_MAX_VALUE))
                                    .min(inputDataAttribute.getValue(InputDataAttribute.REAL_MIN_VALUE))
                                    .getTextFormatter()
                    );
                    break;
                case STRING:
                    control.getProperties().put(FXVK.VK_TYPE_PROP_KEY, FXVK.Type.TEXT.ordinal());
                    control.setTextFormatter(
                            FXTextFormatterUtil.limited(inputDataAttribute.getValue(InputDataAttribute.CHARACTER_LIMIT))
                    );
                    break;
            }

            control.setText(""); //When this changes, to avoid problems clear the text
        }

        var baseAttribute = AttributeFetcher.fetch(attributeMap, BaseAttribute.class);
        if(baseAttribute != null)
        {
            var skin = control.getSkin(); //Seems like setting the skin even if already exists causes some... problems
            if (skin == null) //Set the skin before to be sure that it has it (It's required after)
            {
                control.setSkin(new TextFieldSkin(control));
            }

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

            //control.setShape(baseAttribute.getValue(BaseAttribute.SHAPE_TYPE));
            control.setAlignment(baseAttribute.getValue(BaseAttribute.TEXT_POSITION));
            control.setFont(baseAttribute.getFont());

            var text = FXNodeUtil.getTextFieldText(control);
            if (text != null)
            {
                //Seems like unbinding stuff that is not meant to cause graphical glitches :(
                text.setUnderline(baseAttribute.getValue(BaseAttribute.UNDERLINE));
                text.fillProperty().bind(baseAttribute.getProperty(BaseAttribute.TEXT_COLOR));
            }

            var caretPath = FXNodeUtil.getCaret(control); //Set this after to have it revert after changing the text fill
            if (caretPath != null)
            {
                caretPath.fillProperty().bind(new SimpleObjectProperty<>(Color.BLACK));
            }
        }
    }
}

        /*
        super.addGlobalAttributeInitializer(new InputDataAttribute(), InputDataAttribute.class, data ->
        {
            var control = data.getControl();
            var attribute = data.getAttribute();

            var type = attribute.getValue(InputDataAttribute.TYPE);
            switch (type)
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
        });

        //STATE SPECIFIC
        super.addAttributeInitializer(new WriteAddressAttribute(), WriteAddressAttribute.class, FunctionalInterfaceUtil.emptyConsumer());

        super.addAttributeInitializer(new BaseAttribute(), BaseAttribute.class, data ->
        {
            var control = data.getControl();

            var skin = control.getSkin(); //Seems like setting the skin even if already exists causes some... problems
            if (skin == null) //Set the skin before to be sure that it has it (It's required after)
            {
                control.setSkin(new TextFieldSkin(control));
            }

            var attribute = data.getAttribute();

            var container = data.getContainerPane();
            if (attribute.getValue(BaseAttribute.ADAPT))
            {
                container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                container.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                container.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            } else
            {
                var width = attribute.getValue(BaseAttribute.WIDTH);
                var height = attribute.getValue(BaseAttribute.HEIGHT);

                container.setPrefSize(width, height);
                container.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                container.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }

            //control.setShape(attribute.getShape());
            control.setAlignment(attribute.getValue(BaseAttribute.TEXT_POSITION));
            control.setFont(attribute.getFont());

            var text = FXNodeUtil.getTextFieldText(control);
            if (text != null)
            {
                //Seems like unbinding stuff that is not meant to cause graphical glitches :(
                text.setUnderline(attribute.getValue(BaseAttribute.UNDERLINE));
                text.fillProperty().bind(attribute.getProperty(BaseAttribute.TEXT_COLOR));
            }

            var caretPath = FXNodeUtil.getCaret(control); //Set this after to have it revert after changing the text fill
            if (caretPath != null)
            {
                caretPath.fillProperty().bind(new SimpleObjectProperty<>(Color.BLACK));
            }
        });*/