package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class BackgroundQuickSetupPanePart extends FXObject implements QuickSetupPanePart
{
    @FXML private ColorPicker backgroundColorPicker;

    private final VBox vBox;
    public BackgroundQuickSetupPanePart() throws IOException
    {
        super("BackgroundQuickSetupPane");

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/backgroundQuickSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean validateControlWrapper(ControlWrapper<?> controlWrapper)
    {
        return controlWrapper.getAttributeTypeManager().hasType(AttributeType.BACKGROUND);
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.BACKGROUND)
                .direct(backgroundColorPicker.valueProperty(), BackgroundAttribute.COLOR);
    }

    @Override
    public void clearControlWrapper()
    {
        backgroundColorPicker.setValue(Color.WHITE);
    }
}
