package parozzz.github.com.simpleplcpanel.hmi.controls.quicksetup.impl;

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
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.setup.panes.FontSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.controls.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class FontQuickSetupPanePart extends FXObject implements QuickSetupPanePart
{
    @FXML private ColorPicker textColorPicker;
    @FXML private ComboBox<Integer> textSizeComboBox;
    @FXML private CheckBox boldCheckBox;
    @FXML private CheckBox italicCheckBox;
    @FXML private CheckBox underlineCheckBox;

    private final VBox vBox;

    public FontQuickSetupPanePart() throws IOException
    {
        super("FontQuickSetupPane");

        vBox = (VBox) FXUtil.loadFXML("quickproperties/fontQuickSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));

        textSizeComboBox.setConverter(new IntegerStringConverter());
        textSizeComboBox.getItems().addAll(FontSetupPane.TEXT_SIZE_DEFAULT_CHOICE);

        
    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean isControlWrapperValid(ControlWrapper<?> controlWrapper)
    {
        return controlWrapper.getAttributeTypeManager().hasType(AttributeType.FONT);
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.FONT)
                .direct(textColorPicker.valueProperty(), FontAttribute.COLOR)
                .direct(textSizeComboBox.valueProperty(), FontAttribute.TEXT_SIZE)
                .direct(boldCheckBox.selectedProperty(), FontAttribute.BOLD)
                .direct(italicCheckBox.selectedProperty(), FontAttribute.ITALIC)
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