package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;

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
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {

    }

    @Override
    public void onNewWrapperState(WrapperState wrapperState)
    {
        vBox.setVisible(AttributeFetcher.hasAttribute(wrapperState, SizeAttribute.class));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(SizeAttribute.class)
                .indirect(widthTextField.textProperty(), Integer::parseInt, Objects::toString, SizeAttribute.WIDTH)
                .indirect(heightTextField.textProperty(), Integer::parseInt, Objects::toString, SizeAttribute.HEIGHT);
    }

    @Override
    public void clear()
    {
        widthTextField.setText("");
        heightTextField.setText("");
    }
}
