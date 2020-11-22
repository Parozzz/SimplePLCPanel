package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
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

    private final VBox mainVBox;
    public BackgroundQuickSetupPane() throws IOException
    {
        super("BackgroundQuickSetupPane");

        this.mainVBox = (VBox) FXUtil.loadFXML("quickproperties/backgroundQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(mainVBox, new Insets(2, 0, 0, 0));
    }

    @Override
    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {

    }

    @Override
    public void onNewWrapperState(WrapperState wrapperState)
    {
        mainVBox.setVisible(AttributeFetcher.hasAttribute(wrapperState, BackgroundAttribute.class));
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {
        stateBinder.builder(BackgroundAttribute.class)
                .direct(backgroundColorPicker.valueProperty(), BackgroundAttribute.BACKGROUND_COLOR);
    }

    @Override
    public void clear()
    {
        backgroundColorPicker.setValue(Color.WHITE);
    }
}
