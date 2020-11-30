package parozzz.github.com.hmi.main.quicksetup;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperStateChangedConsumer;
import parozzz.github.com.hmi.main.quicksetup.impl.*;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class QuickSetupVBox extends FXController implements ControlWrapperSpecific
{
    private final ScrollPane mainScrollPane;

    private final StateSelectionQuickSetupPane stateSelectionQuickSetupPane;
    private final GenericQuickSetupPane genericQuickSetupPane;
    private final SizeQuickSetupPane sizeQuickSetupPane;
    private final FontQuickSetupPane fontQuickSetupPane;
    private final BackgroundQuickSetupPane backgroundQuickSetupPane;
    private final TextQuickSetupPane textQuickSetupPane;

    private final QuickSetupStateBinder stateBinder;
    private final Set<QuickSetupPane> quickSetupPaneSet;

    private final ChangeListener<Boolean> validControlWrapperListener;
    private final Consumer<Object> attributesUpdatedConsumer;
    private final WrapperStateChangedConsumer stateChangeConsumer;

    private ControlWrapper<?> selectedControlWrapper;

    public QuickSetupVBox() throws IOException
    {
        super("QuickProperties");

        this.stateBinder = new QuickSetupStateBinder(this);
        this.mainScrollPane = new ScrollPane();

        this.addFXChild(this.stateSelectionQuickSetupPane = new StateSelectionQuickSetupPane())
                .addFXChild(this.genericQuickSetupPane = new GenericQuickSetupPane())
                .addFXChild(this.sizeQuickSetupPane = new SizeQuickSetupPane())
                .addFXChild(this.fontQuickSetupPane = new FontQuickSetupPane())
                .addFXChild(this.backgroundQuickSetupPane = new BackgroundQuickSetupPane())
                .addFXChild(this.textQuickSetupPane = new TextQuickSetupPane());

        this.quickSetupPaneSet = new HashSet<>();

        this.validControlWrapperListener = (observableValue, oldValue, newValue) ->
        {
            if(!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        this.attributesUpdatedConsumer = involvedObject ->
        {
            if(involvedObject != this)
            {
                stateBinder.setIgnoreAttributeUpdate(true);
                stateBinder.refreshValues();
                stateBinder.setIgnoreAttributeUpdate(false);
            }
        };
        this.stateChangeConsumer = (newState, oldState, state) ->
                stateSelectionQuickSetupPane.changeState(newState);
    }

    @Override
    public void setup()
    {
        super.setup();

        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));

        var scrollVBox = new VBox();
        this.computeQuickSetupPane(scrollVBox,
                stateSelectionQuickSetupPane, genericQuickSetupPane,
                sizeQuickSetupPane, fontQuickSetupPane, backgroundQuickSetupPane, textQuickSetupPane
        );

        scrollVBox.setMinSize(0, 0);
        scrollVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        mainScrollPane.setContent(scrollVBox);

        stateSelectionQuickSetupPane.setStateChangeConsumer(wrapperState ->
        {
            if(selectedControlWrapper == null || wrapperState == null)
            {
                stateBinder.setBoundWrapperState(null);
                return;
            }

            selectedControlWrapper.getStateMap().changeCurrentState(wrapperState);

            stateBinder.setBoundWrapperState(wrapperState);
            quickSetupPaneSet.forEach(quickSetupPane -> quickSetupPane.onNewWrapperState(wrapperState));
        });
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

    public void refreshValuesIfSelected(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null && selectedControlWrapper == controlWrapper)
        {
            stateBinder.refreshValues();
        }
    }


    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.validProperty().removeListener(validControlWrapperListener);
            selectedControlWrapper.removeAttributesUpdatedConsumer(attributesUpdatedConsumer);
            selectedControlWrapper.getStateMap().removeStateValueChangedConsumer(stateChangeConsumer);
        }

        this.selectedControlWrapper = controlWrapper;
        if(controlWrapper == null)
        {
            stateBinder.setBoundWrapperState(null); //Without this, values on the control state are cleared on selection change
            quickSetupPaneSet.forEach(QuickSetupPane::clear);
            return;
        }

        controlWrapper.validProperty().addListener(validControlWrapperListener);
        controlWrapper.addAttributesUpdatedConsumer(attributesUpdatedConsumer);
        controlWrapper.getStateMap().addStateValueChangedConsumer(stateChangeConsumer);
        quickSetupPaneSet.forEach(quickSetupPane ->
        {
            quickSetupPane.getParent().setVisible(true); //Uphere is better. Below it could hide it again.
            quickSetupPane.onNewControlWrapper(controlWrapper);
        });
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    void updateSelectedWrapperAttributes()
    {
        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.applyAttributes(this);
        }
    }

    private void computeQuickSetupPane(VBox vBox, QuickSetupPane... quickSetupPanes)
    {
        for(var quickSetupPane : quickSetupPanes)
        {
            quickSetupPane.addBinders(stateBinder);

            quickSetupPaneSet.add(quickSetupPane);
            vBox.getChildren().add(quickSetupPane.getParent());
        }
    }

}
