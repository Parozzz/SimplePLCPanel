package parozzz.github.com.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.attribute.AttributeType;
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
        super(setupPage, "SizeSetupPane", "Size", AttributeType.SIZE);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/sizeSetupPane.fxml", this);
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

}
