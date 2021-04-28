package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperGenericAttributeUpdateConsumer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperStateChangedConsumer;
import parozzz.github.com.simpleplcpanel.hmi.main.quicksetup.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class QuickSetupVBox extends FXController implements ControlWrapperSpecific
{
    private final VBox vBox;
    private final ScrollPane mainScrollPane;

    private final StateSelectionQuickSetupPane stateSelectionQuickSetupPane;
    private final GenericQuickSetupPane genericQuickSetupPane;
    private final SizeQuickSetupPane sizeQuickSetupPane;
    private final FontQuickSetupPane fontQuickSetupPane;
    private final BackgroundQuickSetupPane backgroundQuickSetupPane;
    private final TextQuickSetupPane textQuickSetupPane;

    private final QuickSetupStateBinder stateBinder;
    private final List<QuickSetupPane> quickSetupPaneList;

    private final ChangeListener<Boolean> validControlWrapperListener;
    private final ControlWrapperGenericAttributeUpdateConsumer attributeUpdatedConsumer;
    private final WrapperStateChangedConsumer stateChangeConsumer;

    private ControlWrapper<?> selectedControlWrapper;

    public QuickSetupVBox() throws IOException
    {
        this.stateBinder = new QuickSetupStateBinder(this);
        this.mainScrollPane = new ScrollPane(this.vBox = new VBox());

        this.addFXChild(this.stateSelectionQuickSetupPane = new StateSelectionQuickSetupPane(this))
                .addFXChild(this.genericQuickSetupPane = new GenericQuickSetupPane())
                .addFXChild(this.sizeQuickSetupPane = new SizeQuickSetupPane())
                .addFXChild(this.fontQuickSetupPane = new FontQuickSetupPane())
                .addFXChild(this.backgroundQuickSetupPane = new BackgroundQuickSetupPane())
                .addFXChild(this.textQuickSetupPane = new TextQuickSetupPane());

        this.quickSetupPaneList = new ArrayList<>();

        this.validControlWrapperListener = (observableValue, oldValue, newValue) ->
        {
            if(!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        this.attributeUpdatedConsumer = updateData ->
        {
            for(var attribute : updateData.getAttributeList())
            {
                var attributeType = attribute.getType();
                stateBinder.loadValueFromControlWrapperOf(attributeType);
            }
        };
        //Since this is trigged also when a state is added/removed it should be gucci here
        this.stateChangeConsumer = (stateMap, oldState, changeType) ->
        {
            switch(changeType)
            {
                case ADD:
                case REMOVE:
                    stateSelectionQuickSetupPane.loadStatesOf(selectedControlWrapper);
                    break;
            }

            stateSelectionQuickSetupPane.changeState(stateMap.getCurrentState());
        };

    }

    @Override
    public void setup()
    {
        super.setup();

        vBox.setMinSize(0, 0);
        vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));

        this.computeQuickSetupPane(genericQuickSetupPane, stateSelectionQuickSetupPane,
                sizeQuickSetupPane, fontQuickSetupPane, backgroundQuickSetupPane, textQuickSetupPane
        );
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        this.setSelectedControlWrapper(null); //Start with a null value (Also set everything invisible)
    }

    public Parent getMainParent()
    {
        return mainScrollPane;
    }

    public WrapperState getSelectedWrapperState()
    {
        return stateSelectionQuickSetupPane.getSelectedWrapperState();
    }

    public void refreshValuesIfSelected(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null && selectedControlWrapper == controlWrapper)
        {
            stateBinder.loadAllValuesFromControlWrapper();
        }
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.validProperty().removeListener(validControlWrapperListener);
            selectedControlWrapper.getAttributeUpdater().removeGenericUpdateConsumer(attributeUpdatedConsumer);
            selectedControlWrapper.getStateMap().removeStateValueChangedConsumer(stateChangeConsumer);
        }

        this.selectedControlWrapper = controlWrapper;
        if(controlWrapper == null)
        {
            quickSetupPaneList.forEach(QuickSetupPane::clearControlWrapper);
            return;
        }

        controlWrapper.validProperty().addListener(validControlWrapperListener);
        controlWrapper.getAttributeUpdater().addGenericUpdateConsumer(attributeUpdatedConsumer);
        controlWrapper.getStateMap().addStateValueChangedConsumer(stateChangeConsumer);

        var children = vBox.getChildren();
        children.clear();
        for(var quickSetupPane : quickSetupPaneList)
        {
            if(quickSetupPane.validateControlWrapper(controlWrapper))
            {
                children.add(quickSetupPane.getParent());
            }
        }

        this.loadAllValuesFromControlWrapper();
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    public void loadAllValuesFromControlWrapper()
    {
        stateBinder.loadAllValuesFromControlWrapper();
    }

    private void computeQuickSetupPane(QuickSetupPane... quickSetupPanes)
    {
        for(var quickSetupPane : quickSetupPanes)
        {
            quickSetupPane.addBinders(stateBinder);
            quickSetupPaneList.add(quickSetupPane);
        }
    }

}
