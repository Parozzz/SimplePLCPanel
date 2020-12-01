package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.Objects;

public class FontQuickSetupPane  extends FXObject implements QuickSetupPane
{
    @FXML private ColorPicker textColorPicker;
    @FXML private TextField textSizeTextField;
    @FXML private CheckBox boldCheckBox;
    @FXML private CheckBox italicCheckBox;
    @FXML private CheckBox underlineCheckBox;

    private final VBox vBox;

    public FontQuickSetupPane() throws IOException
    {
        super("FontQuickSetupPane");

        vBox = (VBox) FXUtil.loadFXML("quickproperties/fontQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));
    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {
        vBox.setVisible(controlWrapper.getAttributeManager().hasType(AttributeType.FONT));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.FONT)
                .direct(textColorPicker.valueProperty(), FontAttribute.TEXT_COLOR)
                .indirect(textSizeTextField.textProperty(), Util::parseIntOrZero, Objects::toString, FontAttribute.FONT_TEXT_SIZE)
                .direct(boldCheckBox.selectedProperty(), FontAttribute.BOLD_WEIGHT)
                .direct(italicCheckBox.selectedProperty(), FontAttribute.ITALIC_POSTURE)
                .direct(underlineCheckBox.selectedProperty(), FontAttribute.UNDERLINE);
    }

    @Override
    public void clear()
    {
        QuickSetupPane.super.clear();

        textColorPicker.setValue(Color.WHITE);
        textSizeTextField.setText("");
        boldCheckBox.setSelected(false);
        italicCheckBox.setSelected(false);
        underlineCheckBox.setSelected(false);
    }
}