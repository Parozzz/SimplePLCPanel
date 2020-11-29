package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediate;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediateType;
import parozzz.github.com.util.Util;

import java.io.IOException;

public final class ValueSetupPane extends SetupPane<ValueAttribute>
{
    @FXML private ChoiceBox<ValueIntermediateType<?>> valueTypeChoiceBox;
    @FXML private TextField multiplierTextField;
    @FXML private TextField offsetTextField;
    @FXML private Slider testSlider;

    private final VBox vBox;

    public ValueSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "ValueSetupPane", "Value", ValueAttribute.class, true);

        vBox = (VBox) FXUtil.loadFXML("setup/valueSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        valueTypeChoiceBox.setConverter(FXUtil.toStringOnlyConverter(valueIntermediateType ->
        {
            if(valueIntermediateType == ValueIntermediateType.SHORT)
            {
                return "Word";
            }
            if (valueIntermediateType == ValueIntermediateType.INTEGER)
            {
                return "Double Word";
            } else if (valueIntermediateType == ValueIntermediateType.LONG)
            {
                return "Quad Word";
            } else if (valueIntermediateType == ValueIntermediateType.FLOAT)
            {
                return "Real";
            } else if(valueIntermediateType ==ValueIntermediateType.DOUBLE)
            {
                return "Double Real";
            }

            var name = valueIntermediateType.getName().toLowerCase();
            return Util.capitalize(name);
        }));
        valueTypeChoiceBox.getItems().addAll(ValueIntermediateType.BOOLEAN,
                ValueIntermediateType.SHORT, ValueIntermediateType.INTEGER, ValueIntermediateType.LONG,
                ValueIntermediateType.FLOAT, ValueIntermediateType.DOUBLE,
                ValueIntermediateType.HEX, ValueIntermediateType.STRING);

        multiplierTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
        multiplierTextField.setEditable(true);

        offsetTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
        offsetTextField.setEditable(true);
/*
        var sliderSkin = new SliderSkin(testSlider);
        testSlider.setSkin(sliderSkin);

        var thumb = (StackPane) testSlider.lookup(".thumb");
        thumb.setPadding(new Insets(-1, -1, -1, -1));
        thumb.setMinSize(45, 30);
        thumb.setPrefSize(45, 30);

        var label = new Label("0.00");
        label.setPadding(new Insets(-1, -1, -1, -1));
        testSlider.valueProperty().addListener((observableValue, oldValue, newValue) ->
                label.setText("" + Util.format(newValue.doubleValue(), 2))
        );


        thumb.getChildren().add(label);

        var track = (StackPane) testSlider.lookup(".track");
        track.setCursor(Cursor.CLOSED_HAND);
        track.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        testSlider.setShowTickMarks(true);
        testSlider.setShowTickLabels(true);
*/
        super.getAttributeChangerList().create(valueTypeChoiceBox.valueProperty(), ValueAttribute.INTERMEDIATE_TYPE)
                .createStringToNumber(multiplierTextField.textProperty(), ValueAttribute.MULTIPLY_BY, Util::parseDoubleOrZero)
                .createStringToNumber(offsetTextField.textProperty(), ValueAttribute.OFFSET, Util::parseDoubleOrZero);

        super.computeProperties();
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }
}
