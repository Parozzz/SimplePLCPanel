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
import parozzz.github.com.hmi.main.quicksetup.QuickSetupVBox;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class StateSelectionQuickSetupPane extends FXObject implements QuickSetupPane
{
    @FXML private ChoiceBox<WrapperState> stateChoiceBox;

    private final QuickSetupVBox quickSetupVBox;
    private final VBox vBox;

    public StateSelectionQuickSetupPane(QuickSetupVBox quickSetupVBox) throws IOException
    {
        super("StateSelectionQuickPropertiesPane");

        this.quickSetupVBox = quickSetupVBox;
        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/statesQuickSetupPane.fxml", this);
    }

    @Override
    public void setup()
    {
        super.setup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));

        stateChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
            {
                return;
            }

            var selectedControlWrapper = quickSetupVBox.getSelectedControlWrapper();
            if (selectedControlWrapper != null)
            {
                selectedControlWrapper.getStateMap().forceCurrentState(newValue);

                quickSetupVBox.loadAllValuesFromControlWrapper();
            }
        });
    }

    public WrapperState getSelectedWrapperState()
    {
        return stateChoiceBox.getValue();
    }

    @Override
    public void addBinders(QuickSetupStateBinder stateBinder)
    {

    }

    public Parent getParent()
    {
        return vBox;
    }

    @Override
    public boolean parseControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if(controlWrapper.isStateless())
        {
            return false;
        }

        this.loadStatesOf(controlWrapper);
        return true;
    }

    public void changeState(WrapperState wrapperState)
    {
        stateChoiceBox.setValue(wrapperState);
    }

    public void loadStatesOf(ControlWrapper<?> controlWrapper)
    {
        var stateMap = controlWrapper.getStateMap();
        stateChoiceBox.setValue(stateMap.getCurrentState());

        var stateChoiceBoxItems = stateChoiceBox.getItems();
        stateChoiceBoxItems.clear();
        stateMap.forEach(stateChoiceBoxItems::add);
    }

    @Override
    public void clearControlWrapper()
    {
        stateChoiceBox.setValue(null);
        stateChoiceBox.getItems().clear();
    }
}
