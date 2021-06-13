package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupPanePart;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.QuickSetupStateBinder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class StateSelectionQuickSetupPanePart extends FXObject implements QuickSetupPanePart
{
    @FXML private ChoiceBox<WrapperState> stateChoiceBox;

    private final QuickSetupPane quickSetupPane;
    private final VBox vBox;

    public StateSelectionQuickSetupPanePart(QuickSetupPane quickSetupPane) throws IOException
    {
        super("StateSelectionQuickPropertiesPane");

        this.quickSetupPane = quickSetupPane;
        this.vBox = (VBox) FXUtil.loadFXML("quickproperties/statesQuickSetupPane.fxml", this);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        VBox.setMargin(vBox, new Insets(2, 0, 0, 0));

        stateChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
            {
                return;
            }

            var selectedControlWrapper = quickSetupPane.getSelectedControlWrapper();
            if (selectedControlWrapper != null)
            {
                selectedControlWrapper.getStateMap().setWrapperState(newValue);
                quickSetupPane.loadAllValuesFromControlWrapper();
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
    public boolean validateControlWrapper(ControlWrapper<?> controlWrapper)
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
