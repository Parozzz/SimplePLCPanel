package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.ButtonDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.InputDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoPane;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;
import java.util.*;

public class ControlWrapperSetupStage
        extends HMIStage<HBox>
        implements ControlWrapperSpecific, UndoRedoPane
{

    @FXML private ListView<WrapperState> stateListView;
    private final ControlWrapperSetupStateListView setupStateListView;

    @FXML private VBox mainStateAttributeVBox;
    @FXML private HBox innerStateAttributesHBox;
    private final List<ControlWrapperSetupPaneButton> stateSetupPaneButtonList;

    @FXML private VBox mainGlobalAttributesVBox;
    @FXML private HBox innerGlobalAttributesHBox;
    private final List<ControlWrapperSetupPaneButton> globalSetupPaneButtonList;

    @FXML private StackPane mainStackPane;
    @FXML private Label mainLabel;
    @FXML private Button closePageButton;

    @FXML private TextField xTextField;
    @FXML private TextField yTextField;
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;
    @FXML private CheckBox adaptSizeCheckBox;

    private final MainEditStage mainEditStage;

    private final Map<SetupPane<?>, ControlWrapperSetupPaneButton> setupPaneButtonMap;
    private final ControlWrapperSetupSizeAndPositionManager sizeAndPositionManager;

    private ControlWrapper<?> selectedControlWrapper;
    private WrapperState mainSelectedState;
    private final Set<WrapperState> secondarySelectedWrapperStateSet;

    private final ChangeListener<? super WrapperState> currentWrapperStateListener =
            (observable, oldValue, newValue) -> this.updateBindingOnSetupPanes();

    public ControlWrapperSetupStage(MainEditStage mainEditStage,
            TagsManager tagsManager, CommunicationDataHolder communicationDataHolder) throws IOException
    {
        super("ControlWrapperSetupStage", "setup/ControlWrapperSetupMain.fxml", HBox.class);
        FXUtil.validateAllFXML(this);

        this.mainEditStage = mainEditStage;

        this.addMultipleFXChild(
                this.setupStateListView = new ControlWrapperSetupStateListView(this, stateListView),
                this.sizeAndPositionManager = new ControlWrapperSetupSizeAndPositionManager(xTextField, yTextField, widthTextField, heightTextField, adaptSizeCheckBox)
        );

        this.setupPaneButtonMap = new IdentityHashMap<>();

        this.stateSetupPaneButtonList = new LinkedList<>();
        this.addStatePaneButton("selectBackgroundButton", new BackgroundSetupPane(this))
                .addStatePaneButton("selectBorderButton", new BorderSetupPane(this))
                .addStatePaneButton("selectFontButton", new FontSetupPane(this))
                .addStatePaneButton("selectTextButton", new TextSetupPane(this))
                .addStatePaneButton("selectChangePageButton", new ChangePageSetupPane(this))
                .addStatePaneButton("selectValueButton", new ValueSetupPane(this))
                .addStatePaneButton("selectWriteTagButton",
                        new AddressSetupPane<>(this, tagsManager, communicationDataHolder, AttributeType.WRITE_ADDRESS)
                );

        this.globalSetupPaneButtonList = new LinkedList<>();
        this.addGlobalPaneButton("selectButtonDataButton", new ButtonDataSetupPane(this))
                .addGlobalPaneButton("selectFieldButton", new InputDataSetupPane(this))
                .addGlobalPaneButton("selectReadTagButton",
                        new AddressSetupPane<>(this, tagsManager, communicationDataHolder, AttributeType.READ_ADDRESS)
                );

        secondarySelectedWrapperStateSet = new HashSet<>();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        this.getStageSetter().setAlwaysOnTop(true)
                .setResizable(false)
                .setOnWindowCloseRequest(windowEvent -> this.setSelectedControlWrapper(null));

        closePageButton.setOnMouseClicked(event ->
                this.setActiveSetupPane(null)
        );

        innerGlobalAttributesHBox.getChildren().clear();
        innerStateAttributesHBox.getChildren().clear();

        this.setSelectedControlWrapper(null); //This is set everything to default.
        this.setActiveSetupPane(null); //This is set everything to default.
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public ControlWrapperSetupStateListView getStateListView()
    {
        return setupStateListView;
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        var oldControlWrapper = selectedControlWrapper;
        if (oldControlWrapper != null)
        {
            oldControlWrapper.getStateMap().currentWrapperStateProperty().removeListener(currentWrapperStateListener);

            //this.setActiveSetupPane(null); //I do not want to reset it, so I can quickly switch values without changing attribute!
            this.setMainSelectedState(null);
            setupStateListView.clearStates();
            //Clear all the undoing for the old wrapper to eliminate undo from another contro.
            UndoRedoManager.getInstance().removeAllUndoActionsWithPane(this);
        }

        selectedControlWrapper = controlWrapper; //This should belong here. Everything below could depend on it!
        sizeAndPositionManager.setControlWrapper(controlWrapper);

        if (controlWrapper != null)
        {
            //This listener need to stay here, so it called with the two methods below this one.
            controlWrapper.getStateMap().currentWrapperStateProperty().addListener(currentWrapperStateListener);

            this.setMainSelectedState(controlWrapper.getStateMap().getCurrentState());
            setupStateListView.loadStates();
        }

        //Update anyway. In case the control wrapper is null, it will just clear them.
        this.updateBindingOnSetupPanes();
        if (controlWrapper == null)
        {
            this.hideStage();
        } else
        {
            this.showStage();
        }
    }


    public WrapperState getMainSelectedState()
    {
        return mainSelectedState;
    }

    public void setMainSelectedState(WrapperState wrapperState)
    {
        //When changing the mainSelectedWrapperStateProperty it will call the listener inside the StateMap
        //of the ControlWrapper updating everything.
        secondarySelectedWrapperStateSet.clear(); //THIS NEED TO BE FIRST. Otherwise, when setting a new value to main
        //all the secondary data will be overwritten by default values!
        //More checks has been added inside the SetupPaneAttributeChanger to
        //avoid this problem, but you never know.
        mainSelectedState = wrapperState;
        if (wrapperState != null)
        {
            Objects.requireNonNull(selectedControlWrapper, "Trying to set a selected state while the selected ControlWrapper is null?");

            //When a WrapperState is set, force it inside the ControlWrapper to allow the listener to be called (And execute the ABOVE).
            selectedControlWrapper.getStateMap().setWrapperState(wrapperState);
        }
    }

    public boolean isSecondarySelectedState(WrapperState wrapperState)
    {
        return secondarySelectedWrapperStateSet.contains(wrapperState);
    }

    public Set<WrapperState> getSecondarySelectedWrapperStateSet()
    {
        return Collections.unmodifiableSet(secondarySelectedWrapperStateSet);
    }

    public void addSecondarySelectedState(WrapperState wrapperState)
    {
        //If there is not a main selection, it will be forced into!
        if (mainSelectedState == null)
        {
            this.setMainSelectedState(wrapperState);
        } else if (mainSelectedState != wrapperState) //Cannot add a main as a secondary!
        {
            secondarySelectedWrapperStateSet.add(wrapperState);
        }
    }

    /**
     * Set the active shown setup pane.
     *
     * @param setupPane The setup pane to show. Set to {@code null} to remove it.
     */
    public void setActiveSetupPane(@Nullable SetupPane<?> setupPane)
    {
        this.clearAllSelectedButtons();

        mainLabel.setText("No Selected");

        var children = mainStackPane.getChildren();
        children.clear();
        if (setupPane != null)
        {
            mainLabel.setText(setupPane.getAttributeType().getName());
            children.add(setupPane.getParent());

            var setupPaneButton = setupPaneButtonMap.get(setupPane);
            if (setupPaneButton != null)
            {
                setupPaneButton.showButtonAsSelected();
            }
        }
    }

    void clearAllSelectedButtons()
    {
        stateSetupPaneButtonList.forEach(ControlWrapperSetupPaneButton::clearButtonSelection);
        globalSetupPaneButtonList.forEach(ControlWrapperSetupPaneButton::clearButtonSelection);
    }

    private void updateBindingOnSetupPanes()
    {
        var selectedControlWrapper = this.getSelectedControlWrapper();

        //This is required otherwise when loading stuff could be undo event if it shouldn't
        UndoRedoManager.getInstance().startIgnoringNextActions();

        //Unbinding all at start. They will be binded afterward in case.
        innerGlobalAttributesHBox.getChildren().clear();
        for (var globalPaneButton : globalSetupPaneButtonList)
        {
            var setupPane = globalPaneButton.getSetupPane();
            setupPane.unbindAll();
            if (setupPane.bindAll(selectedControlWrapper))
            {
                innerGlobalAttributesHBox.getChildren().add(globalPaneButton.getButton());
            }
        }

        innerStateAttributesHBox.getChildren().clear();
        for (var statePaneButton : stateSetupPaneButtonList)
        {
            var setupPane = statePaneButton.getSetupPane();
            setupPane.unbindAll();
            if (setupPane.bindAll(selectedControlWrapper))
            {
                innerStateAttributesHBox.getChildren().add(statePaneButton.getButton());
            }
        }

        UndoRedoManager.getInstance().stopIgnoringNextActions();
    }

    private ControlWrapperSetupStage addStatePaneButton(String id, SetupPane<?> setupPane)
    {
        try
        {
            var setupPaneButton = new ControlWrapperSetupPaneButton(this, innerStateAttributesHBox, id, setupPane);
            this.addFXChild(setupPaneButton);
            this.stateSetupPaneButtonList.add(setupPaneButton);

            setupPaneButtonMap.put(setupPane, setupPaneButton);
        } catch (Exception ex)
        {
            MainLogger.getInstance().error("Error while loading a State SetupPane", ex, this);
        }

        return this;
    }

    private ControlWrapperSetupStage addGlobalPaneButton(String id, SetupPane<?> setupPane)
    {
        try
        {
            var setupPaneButton = new ControlWrapperSetupPaneButton(this, innerGlobalAttributesHBox, id, setupPane);
            this.addFXChild(setupPaneButton);
            this.globalSetupPaneButtonList.add(setupPaneButton);

            setupPaneButtonMap.put(setupPane, setupPaneButton);
        } catch (Exception ex)
        {
            MainLogger.getInstance().error("Error while loading a Global SetupPane", ex, this);
        }

        return this;
    }

    @Override
    public void undoActionExecuted()
    {

    }
}
