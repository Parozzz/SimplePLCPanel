package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.util.Objects;

public final class SizeQuickSetupPanePart extends FXObject implements QuickSetupPanePart
{
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;

    private final VBox vBox;

    public SizeQuickSetupPanePart() throws IOException
    {
        super("BaseQuickPropertiesPane");

        vBox = (VBox) FXUtil.loadFXML("quickproperties/sizeQuickSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));
    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean validateControlWrapper(ControlWrapper<?> controlWrapper)
    {
        return controlWrapper.getAttributeTypeManager().hasType(AttributeType.SIZE);
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.SIZE)
                .indirect(widthTextField.textProperty(), Util::parseIntOrZero, Objects::toString, SizeAttribute.WIDTH)
                .indirect(heightTextField.textProperty(), Util::parseIntOrZero, Objects::toString, SizeAttribute.HEIGHT);
    }

    @Override
    public void clearControlWrapper()
    {
        widthTextField.setText("");
        heightTextField.setText("");
    }
}
