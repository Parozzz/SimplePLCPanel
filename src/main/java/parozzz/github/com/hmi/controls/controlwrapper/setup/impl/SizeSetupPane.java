package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;

@SuppressWarnings("unused")
public final class SizeSetupPane extends SetupPane<SizeAttribute>
{
    @FXML private CheckBox adaptCheckbox;
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;

    private final VBox mainVBox;

    public SizeSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "SizeSetupPane", "Size", SizeAttribute.class);

        this.mainVBox = (VBox) FXUtil.loadFXML("setupv2/sizeSetupPaneV2.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        widthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        heightTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));

        super.getAttributeChangerList().create(adaptCheckbox.selectedProperty(), SizeAttribute.ADAPT)
                .createStringToNumber(widthTextField.textProperty(), SizeAttribute.WIDTH, Util::parseIntOrZero)
                .createStringToNumber(heightTextField.textProperty(), SizeAttribute.HEIGHT, Util::parseIntOrZero);

        super.computeProperties();
    }

    @Override
    public void setDefault()
    {
        super.setDefault();

        widthTextField.setText("120");
        heightTextField.setText("80");
    }


    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public boolean hasAttribute(AttributeMap attributeMap)
    {
        return attributeMap.hasAttribute(SizeAttribute.class);
    }

    /*
    @Override
    protected void setData(BaseAttribute attribute)
    {
        attribute.textColorProperty = textColorPicker.getValue();
        attribute.underlineProperty = underlineButton.isSelected();

        attribute.fontNameProperty = fontChoiceBox.getSelectionModel().getSelectedItem();
        attribute.fontWeightProperty = boldTextStyleButton.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        attribute.fontPostureProperty = italicTextStyleButton.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        attribute.fontTextSizeProperty = this.getTextSize();

        attribute.shapeTypeProperty = shapeChoiceBox.getValue();

        attribute.adaptProperty = adaptCheckbox.isSelected();
        attribute.widthProperty = Integer.parseInt(widthTextField.getText());
        attribute.heightProperty = Integer.parseInt(heightTextField.getText());
    }

    @Override
    protected void loadData(BaseAttribute attribute)
    {
        underlineButton.setSelected(attribute.underlineProperty);
        textColorPicker.setValue(attribute.textColorProperty);

        var index = fontChoiceBox.getItems().indexOf(attribute.fontNameProperty);
        if (index != -1)
        {
            fontChoiceBox.getSelectionModel().select(index);
        }

        boldTextStyleButton.setSelected(attribute.fontWeightProperty == FontWeight.BOLD);
        italicTextStyleButton.setSelected(attribute.fontPostureProperty == FontPosture.ITALIC);
        textSizeComboBox.setValue((int) attribute.fontTextSizeProperty);

        shapeChoiceBox.setValue(attribute.shapeTypeProperty);

        adaptCheckbox.setSelected(attribute.adaptProperty);
        widthTextField.setText("" + (int) attribute.widthProperty);
        heightTextField.setText("" + (int) attribute.heightProperty);
    }*/
}
