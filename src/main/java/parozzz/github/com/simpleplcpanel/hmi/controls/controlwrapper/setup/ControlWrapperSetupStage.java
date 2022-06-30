package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.ButtonDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.InputDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.state.ControlWrapperSetupStateListView;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.state.ControlWrapperStateSelection;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoPane;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Util;
import parozzz.github.com.simpleplcpanel.util.XmlTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ControlWrapperSetupStage
        extends HMIStage<HBox>
        implements ControlWrapperSpecific, UndoRedoPane
{

    private static String generateSVGPath(String imageName)
    {
        return "images/ControlSetup/" + imageName + ".svg";
    }

    @FXML private ListView<WrapperState> stateListView;
    private final ControlWrapperSetupStateListView setupStateListView;


    @FXML private VBox stateMainVBox;
    @FXML private HBox stateButtonsHBox;
    private final List<ControlWrapperSetupPaneButton> stateButtonList;


    @FXML private VBox globalMainVBox;
    @FXML private HBox globalButtonsHBox;
    private final List<ControlWrapperSetupPaneButton> globalButtonList;


    @FXML private StackPane mainStackPane;
    @FXML private Label mainLabel;
    @FXML private Button closePageButton;


    private final MainEditStage mainEditStage;

    private final Map<SetupPane<?>, ControlWrapperSetupPaneButton> setupPaneButtonMap;
    private final ControlWrapperSetupSizeAndPositionManager sizeAndPositionManager;
    private final ControlWrapperStateSelection stateSelection;
    private ControlWrapper<?> selectedControlWrapper;

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
                this.sizeAndPositionManager = new ControlWrapperSetupSizeAndPositionManager(this)
        );

        this.setupPaneButtonMap = new IdentityHashMap<>();

        this.stateButtonList = new LinkedList<>();
        this.addStatePaneButton("selectBackgroundButton", generateSVGPath("background"), new BackgroundSetupPane(this))
                .addStatePaneButton("selectBorderButton", generateSVGPath("border"), new BorderSetupPane(this))
                .addStatePaneButton("selectFontButton", generateSVGPath("font"), new FontSetupPane(this))
                .addStatePaneButton("selectTextButton",  generateSVGPath("text"), new TextSetupPane(this))
                .addStatePaneButton("selectChangePageButton",  generateSVGPath("change_page"), new ChangePageSetupPane(this))
                .addStatePaneButton("selectValueButton", generateSVGPath("maths"), new ValueSetupPane(this))
                .addStatePaneButton("selectWriteTagButton", generateSVGPath("tag_read"),
                        new AddressSetupPane<>(this, tagsManager, communicationDataHolder, AttributeType.WRITE_ADDRESS)
                );

        this.globalButtonList = new LinkedList<>();
        this.addGlobalPaneButton("selectButtonDataButton", generateSVGPath("settings"), new ButtonDataSetupPane(this))
                .addGlobalPaneButton("selectFieldButton",generateSVGPath("settings"), new InputDataSetupPane(this))
                .addGlobalPaneButton("selectReadTagButton", generateSVGPath("tag_write"),
                        new AddressSetupPane<>(this, tagsManager, communicationDataHolder, AttributeType.READ_ADDRESS)
                );


        this.stateSelection = new ControlWrapperStateSelection(this);
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

        globalButtonsHBox.getChildren().clear();
        stateButtonsHBox.getChildren().clear();

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

    public ControlWrapperStateSelection getStateSelection()
    {
        return stateSelection;
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        var oldControlWrapper = selectedControlWrapper;
        if (oldControlWrapper != null)
        {
            oldControlWrapper.getStateMap().currentWrapperStateProperty().removeListener(currentWrapperStateListener);

            //this.setActiveSetupPane(null); //I do not want to reset it, so I can quickly switch values without changing attribute!
            stateSelection.setMainState(null);
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

            stateSelection.setMainState(controlWrapper.getStateMap().getCurrentState());
            setupStateListView.loadStates();
        }

        //Update anyway. In case the control wrapper is null, it will just clear them.
        this.updateBindingOnSetupPanes();
        if (controlWrapper == null)
        {
            this.hideStage();
        }
        else
        {
            this.showStage();
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
        stateButtonList.forEach(ControlWrapperSetupPaneButton::clearButtonSelection);
        globalButtonList.forEach(ControlWrapperSetupPaneButton::clearButtonSelection);
    }

    private void updateBindingOnSetupPanes()
    {
        var selectedControlWrapper = this.getSelectedControlWrapper();

        //This is required otherwise when loading stuff could be undo event if it shouldn't
        UndoRedoManager.getInstance().startIgnoringNextActions();

        //Unbinding all at start. They will be binded afterward in case.
        globalButtonsHBox.getChildren().clear();
        for (var globalPaneButton : globalButtonList)
        {
            var setupPane = globalPaneButton.getSetupPane();
            setupPane.unbindAll();
            if (setupPane.bindAll(selectedControlWrapper))
            {
                globalButtonsHBox.getChildren().add(globalPaneButton.getButton());
            }
        }

        stateButtonsHBox.getChildren().clear();
        for (var statePaneButton : stateButtonList)
        {
            var setupPane = statePaneButton.getSetupPane();
            setupPane.unbindAll();
            if (setupPane.bindAll(selectedControlWrapper))
            {
                stateButtonsHBox.getChildren().add(statePaneButton.getButton());
            }
        }

        UndoRedoManager.getInstance().stopIgnoringNextActions();
    }

    private ControlWrapperSetupStage addStatePaneButton(String buttonID, String svgImageResourcePath, SetupPane<?> setupPane)
    {
        try
        {
            var button = FXUtil.findNestedChild(this, buttonID, Button.class);
            Objects.requireNonNull(button, "Cannot find state button.");

            var setupPaneButton = new ControlWrapperSetupPaneButton(this, button, svgImageResourcePath, setupPane);
            this.addFXChild(setupPaneButton);
            this.stateButtonList.add(setupPaneButton);

            setupPaneButtonMap.put(setupPane, setupPaneButton);
        }
        catch (Exception ex)
        {
            MainLogger.getInstance().error("Error while loading a State SetupPane", ex, this);
        }

        return this;
    }

    private ControlWrapperSetupStage addGlobalPaneButton(String buttonID, String svgImageResourcePath, SetupPane<?> setupPane)
    {
        try
        {
            var button = FXUtil.findNestedChild(this, buttonID, Button.class);
            Objects.requireNonNull(button, "Cannot find global button.");

            var setupPaneButton = new ControlWrapperSetupPaneButton(this, button, svgImageResourcePath, setupPane);
            this.addFXChild(setupPaneButton);
            this.globalButtonList.add(setupPaneButton);

            setupPaneButtonMap.put(setupPane, setupPaneButton);
        }
        catch (Exception ex)
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
