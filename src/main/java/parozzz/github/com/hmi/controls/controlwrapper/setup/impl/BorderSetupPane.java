package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.BorderAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.EnumStringConverter;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

public class BorderSetupPane extends SetupPane<BorderAttribute>
{
    @FXML private ColorPicker borderColorPicker;
    @FXML private ChoiceBox<BorderAttribute.StrokeStyle> borderStyleChoiceBox;
    @FXML private TextField borderWidthTextField;
    @FXML private TextField cornerRadiiTextField;

    private final VBox vBox;

    public BorderSetupPane(ControlWrapperSetupStage setupStage) throws IOException
    {
        super(setupStage, "BorderSetupPane", "Border", BorderAttribute.class, true);

        this.vBox = (VBox) FXUtil.loadFXML("setup/borderSetupPane.fxml", this);
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public void setup()
    {
        super.setup();

        borderStyleChoiceBox.setConverter(new EnumStringConverter<>(BorderAttribute.StrokeStyle.class).setCapitalize());
        borderStyleChoiceBox.getItems().addAll(BorderAttribute.StrokeStyle.values());

        borderWidthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        cornerRadiiTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(2));

        super.getAttributeChangerList().create(borderColorPicker.valueProperty(), BorderAttribute.COLOR)
                .createStringToNumber(borderWidthTextField.textProperty(), BorderAttribute.WIDTH, Util::parseIntOrZero)
                .create(borderStyleChoiceBox.valueProperty(), BorderAttribute.STROKE_STYLE)
                .createStringToNumber(cornerRadiiTextField.textProperty(), BorderAttribute.CORNER_RADII, Util::parseIntOrZero);

        super.computeProperties();
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        borderColorPicker.setValue(Color.BLACK);
        borderWidthTextField.setText("1");
        borderStyleChoiceBox.setValue(BorderAttribute.StrokeStyle.SOLID);
        cornerRadiiTextField.setText("1");
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(BorderAttribute.class);
    }

}
