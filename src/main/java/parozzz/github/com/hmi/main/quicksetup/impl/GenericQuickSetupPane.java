package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class GenericQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML private TextField typeTextField;

    private final VBox mainVBox;
    public GenericQuickSetupPane() throws IOException
    {
        super("GenericQuickProperties");

        this.mainVBox = (VBox) FXUtil.loadFXML("quickproperties/genericQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(mainVBox, new Insets(2, 0, 0,  0));

        typeTextField.setEditable(false);
    }

    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public boolean parseControlWrapper(ControlWrapper<?> controlWrapper)
    {
        typeTextField.setText(controlWrapper.getType().getUserFriendlyName());
        return true;
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {

    }

    @Override
    public void clearControlWrapper()
    {
        typeTextField.setText("");
    }
}
