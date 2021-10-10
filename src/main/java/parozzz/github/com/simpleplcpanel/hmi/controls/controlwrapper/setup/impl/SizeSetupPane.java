package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.ControlWrapperSetupStage;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.SetupPane;
import parozzz.github.com.simpleplcpanel.hmi.util.FXTextFormatterUtil;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;

@SuppressWarnings("unused")
public final class SizeSetupPane extends SetupPane<SizeAttribute>
{
    @FXML
    private CheckBox adaptCheckbox;
    @FXML
    private TextField widthTextField;
    @FXML
    private TextField heightTextField;
    @FXML
    private TextField paddingTextField;

    private final VBox mainVBox;

    public SizeSetupPane(ControlWrapperSetupStage setupPage) throws IOException
    {
        super(setupPage, "SizeSetupPane", AttributeType.SIZE);

        this.mainVBox = (VBox) FXUtil.loadFXML("setup/sizeSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        widthTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        heightTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));
        paddingTextField.setTextFormatter(FXTextFormatterUtil.simpleInteger(3));

        super.getAttributeChangerList().create(adaptCheckbox.selectedProperty(), SizeAttribute.ADAPT)
                .createStringToNumber(widthTextField.textProperty(), SizeAttribute.WIDTH, Util::parseIntOrZero)
                .createStringToNumber(heightTextField.textProperty(), SizeAttribute.HEIGHT, Util::parseIntOrZero)
                .createStringToNumber(paddingTextField.textProperty(), SizeAttribute.PADDING, Util::parseIntOrZero);

        super.computeProperties();
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }
}
