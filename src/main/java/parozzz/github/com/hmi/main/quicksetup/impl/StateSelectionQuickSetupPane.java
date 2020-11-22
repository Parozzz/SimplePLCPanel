package parozzz.github.com.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.functionalinterface.FunctionalInterfaceUtil;

import java.io.IOException;
import java.util.function.Consumer;

public final class StateSelectionQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML private ChoiceBox<WrapperState> stateChoiceBox;

    private final VBox mainVBox;
    private Consumer<WrapperState> stateChangeConsumer = FunctionalInterfaceUtil.emptyConsumer();
    public StateSelectionQuickSetupPane() throws IOException
    {
        super("StateSelectionQuickPropertiesPane");

        this.mainVBox = (VBox) FXUtil.loadFXML("quickproperties/statesQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(mainVBox, new Insets(2, 0, 0,  0));

        stateChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> stateChangeConsumer.accept(newValue));
    }

    public void setStateChangeConsumer(Consumer<WrapperState> consumer)
    {
        this.stateChangeConsumer = consumer;
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {

    }

    @Override
    public void clear()
    {
        stateChoiceBox.getItems().clear();
    }

    public Parent getParent()
    {
        return mainVBox;
    }

    @Override
    public void onNewControlWrapper(ControlWrapper<?> controlWrapper)
    {
        var items = stateChoiceBox.getItems();
        items.clear();

        var stateMap = controlWrapper.getStateMap();
        stateMap.forEach(items::add);
        stateChoiceBox.getSelectionModel().select(stateMap.getCurrentState());
    }

    @Override
    public void onNewWrapperState(WrapperState wrapperState)
    {

    }
}
