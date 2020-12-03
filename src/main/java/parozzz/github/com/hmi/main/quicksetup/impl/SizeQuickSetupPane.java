package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.Objects;

public final class SizeQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;

    private final VBox vBox;

    public SizeQuickSetupPane() throws IOException
    {
        super("BaseQuickPropertiesPane");

        vBox = (VBox) FXUtil.loadFXML("quickproperties/sizeQuickSetupPane.fxml", this);
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
    public boolean parseControlWrapper(ControlWrapper<?> controlWrapper)
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
