package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.impl.ValueAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediateType;
import parozzz.github.com.util.Util;

import java.io.IOException;

public final class ValueSetupPane extends SetupPane<ValueAttribute>
{
    @FXML private ChoiceBox<ValueIntermediateType<?>> valueTypeChoiceBox;
    @FXML private TextField multiplyByTextField;
    @FXML private TextField offsetTextField;
    @FXML private Slider testSlider;

    private final AnchorPane mainAnchorPane;

    public ValueSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "ValueSetupPane", "Value", ValueAttribute.class);

        mainAnchorPane = (AnchorPane) FXUtil.loadFXML("setup/valueSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        valueTypeChoiceBox.setConverter(FXUtil.toStringOnlyConverter(valueIntermediateType ->
        {
            if (valueIntermediateType == ValueIntermediateType.SHORT)
            {
                return "Short Integer";
            } else if (valueIntermediateType == ValueIntermediateType.LONG)
            {
                return "Long Integer";
            } else if (valueIntermediateType == ValueIntermediateType.DOUBLE)
            {
                return "Double Float";
            }

            var name = valueIntermediateType.getName().toLowerCase();
            return Util.capitalize(name);
        }));
        valueTypeChoiceBox.getItems().addAll(ValueIntermediateType.values());

        multiplyByTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
        multiplyByTextField.setEditable(true);

        offsetTextField.setTextFormatter(FXTextFormatterUtil.doubleBuilder().getTextFormatter());
        offsetTextField.setEditable(true);

        var sliderSkin = new SliderSkin(testSlider);
        testSlider.setSkin(sliderSkin);

        try
        {
            var thumbField = SliderSkin.class.getDeclaredField("thumb");
            thumbField.trySetAccessible();

            var thumb = (StackPane) thumbField.get(sliderSkin);
            thumb.setPadding(new Insets(-1, -1, -1, -1));
            thumb.setMinSize(45, 30);
            thumb.setPrefSize(45, 30);

            var label = new Label("0.00");
            label.setPadding(new Insets(-1, -1, -1, -1));
            testSlider.valueProperty().addListener((observableValue, oldValue, newValue) ->
            {
                var doubleValue = newValue.doubleValue();
                label.setText("" + Util.format(doubleValue, 2));
            });
            thumb.getChildren().add(label);


            var trackField = SliderSkin.class.getDeclaredField("track");
            trackField.trySetAccessible();

            var track = (StackPane) trackField.get(sliderSkin);
            track.setCursor(Cursor.CLOSED_HAND);
            track.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        } catch (NoSuchFieldException | IllegalAccessException exception)
        {
            exception.printStackTrace();
        }

        testSlider.setShowTickMarks(true);
        testSlider.setShowTickLabels(true);

        super.getAttributeChangerList().create(valueTypeChoiceBox.valueProperty(), ValueAttribute.INTERMEDIATE_TYPE)
                .createStringToNumber(multiplyByTextField.textProperty(), ValueAttribute.MULTIPLY_BY, Util::parseDoubleOrZero)
                .createStringToNumber(offsetTextField.textProperty(), ValueAttribute.OFFSET, Util::parseDoubleOrZero);

        super.computeProperties();
    }

    @Override
    public Parent getMainParent()
    {
        return mainAnchorPane;
    }
}
