package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.hmi.attribute.impl.BaseAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.Objects;

public final class BaseQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private TextField textSizeTextField;
    @FXML
    private CheckBox boldCheckBox;
    @FXML
    private CheckBox italicCheckBox;
    @FXML
    private CheckBox underlineCheckBox;
    @FXML
    private TextField widthTextField;
    @FXML
    private TextField heightTextField;

    private final VBox mainVBox;

    public BaseQuickSetupPane() throws IOException
    {
        super("BaseQuickPropertiesPane");

        mainVBox = (VBox) FXUtil.loadFXML("quickproperties/baseQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(mainVBox, new Insets(2, 0, 0, 0));
    }

    public Parent getMainParent()
    {
        return mainVBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {

    }

    @Override
    public void onNewWrapperState(WrapperState wrapperState)
    {
        mainVBox.setVisible(AttributeFetcher.hasAttribute(wrapperState, BaseAttribute.class));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(BaseAttribute.class)
                .direct(textColorPicker.valueProperty(), BaseAttribute.TEXT_COLOR)
                .indirect(textSizeTextField.textProperty(), Integer::parseInt, Objects::toString, BaseAttribute.FONT_TEXT_SIZE)
                .direct(boldCheckBox.selectedProperty(), BaseAttribute.BOLD_WEIGHT)
                .direct(italicCheckBox.selectedProperty(), BaseAttribute.ITALIC_POSTURE)
                .direct(underlineCheckBox.selectedProperty(), BaseAttribute.UNDERLINE)
                .indirect(widthTextField.textProperty(), Integer::parseInt, Objects::toString, BaseAttribute.WIDTH)
                .indirect(heightTextField.textProperty(), Integer::parseInt, Objects::toString, BaseAttribute.HEIGHT);
    }

    @Override
    public void clear()
    {
        textColorPicker.setValue(Color.WHITE);
        textSizeTextField.setText("");
        boldCheckBox.setSelected(false);
        italicCheckBox.setSelected(false);
        underlineCheckBox.setSelected(false);
        widthTextField.setText("");
        heightTextField.setText("");
    }
}
