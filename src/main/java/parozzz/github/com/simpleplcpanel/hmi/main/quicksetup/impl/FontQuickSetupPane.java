package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.FontAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.FontSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class FontQuickSetupPane  extends FXObject implements QuickSetupPane
{
    @FXML private ColorPicker textColorPicker;
    @FXML private ComboBox<Integer> textSizeComboBox;
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

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(FontSetupPane.TEXT_SIZE_DEFAULT_CHOICE);

        
    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean validateControlWrapper(ControlWrapper<?> controlWrapper)
    {
        return controlWrapper.getAttributeTypeManager().hasType(AttributeType.FONT);
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.FONT)
                .direct(textColorPicker.valueProperty(), FontAttribute.TEXT_COLOR)
                .direct(textSizeComboBox.valueProperty(), FontAttribute.FONT_TEXT_SIZE)
                .direct(boldCheckBox.selectedProperty(), FontAttribute.BOLD_WEIGHT)
                .direct(italicCheckBox.selectedProperty(), FontAttribute.ITALIC_POSTURE)
                .direct(underlineCheckBox.selectedProperty(), FontAttribute.UNDERLINE);
    }

    @Override
    public void clearControlWrapper()
    {
        textColorPicker.setValue(Color.WHITE);
        textSizeComboBox.setValue(12);
        boldCheckBox.setSelected(false);
        italicCheckBox.setSelected(false);
        underlineCheckBox.setSelected(false);
    }
}