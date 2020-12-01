package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class BackgroundQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML private ColorPicker backgroundColorPicker;

    private final VBox vBox;
    public BackgroundQuickSetupPane() throws IOException
    {
        super("BackgroundQuickSetupPane");

        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/backgroundQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));
    }

    @Override
    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {
        vBox.setVisible(controlWrapper.getAttributeManager().hasType(AttributeType.BACKGROUND));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(AttributeType.BACKGROUND)
                .direct(backgroundColorPicker.valueProperty(), BackgroundAttribute.BACKGROUND_COLOR);
    }

    @Override
    public void clear()
    {
        QuickSetupPane.super.clear();

        backgroundColorPicker.setValue(Color.WHITE);
    }
}
