package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.extra.SelectAndMultipleWrite;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.BackgroundSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.BaseSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.TextSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.ValueSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control.ButtonDataSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control.InputDataSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.extrafunction.ExtraFunctionSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.page.BorderPaneHMIStage;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ControlWrapperSetupStage extends BorderPaneHMIStage
{
    @FXML
    private ChoiceBox<WrapperState> stateSelectionChoiceBox;
    @FXML
    private MenuItem deleteStateMenuItem;
    @FXML
    private MenuItem createStateMenuItem;

    @FXML
    private AnchorPane createStateAnchorPane;

    @FXML
    private StackPane previewStackPane;

    @FXML
    private TabPane globalTabPane;
    @FXML
    private TabPane stateTabPane;

    @FXML
    private ToggleButton selectMultipleToggleButton;
    @FXML
    private ImageView selectMultipleImageView;

    @FXML
    private Button writeToAllStateButton;
    @FXML
    private ImageView writeToAllStateImageView;

    private final ControlContainerPane controlContainerPane;

    private final SetupPaneList stateSetupPaneList;
    private final SetupPaneList globalSetupPaneList;
    private ControlWrapper<?> selectedControlWrapper;

    private final SelectAndMultipleWrite selectAndMultipleWrite;

    private Tab lastStateTabBeforeClosing;
    private Tab lastGlobalTabBeforeClosing;

    public ControlWrapperSetupStage(ControlContainerPane controlContainerPane) throws IOException
    {
        super("ControlWrapperSetupPage", "setup/mainSetupPage.fxml");

        this.controlContainerPane = controlContainerPane;

        //Little pane that appears when selecting context menu "Create" in the upper pane
        this.addFXChild(new StateCreationPane(this, createStateAnchorPane));

        this.stateSetupPaneList = new SetupPaneList();
        this.globalSetupPaneList = new SetupPaneList();

        this.addFXChild(selectAndMultipleWrite = new SelectAndMultipleWrite(this,
                selectMultipleToggleButton, selectMultipleImageView,
                writeToAllStateButton, writeToAllStateImageView), false);
    }

    @Override
    public void setup()
    {
        super.setup();

        try
        {
            stateSetupPaneList.add(new BaseSetupPane(this)).
                    add(new TextSetupPane(this)).
                    add(new ValueSetupPane(this)).
                    add(new BackgroundSetupPane(this)).
                    add(new AddressSetupPane<>(this, "WriteAddress", WriteAddressAttribute.class, false));

            globalSetupPaneList.add(new ButtonDataSetupPane(this)) //I want this first! >:)
                    .add(new InputDataSetupPane(this))
                    .add(new ExtraFunctionSetupPane(this))
                    .add(new AddressSetupPane<>(this, "ReadAddress", ReadAddressAttribute.class, true)); //I want this last! >:(
        }
        catch(IOException exception)
        {
            Logger.getLogger(ControlWrapperSetupStage.class.getSimpleName())
                    .log(Level.WARNING, "Exception while loading ControlWrapperSetupPage", exception);
        }

        this.getStageSetter().setAlwaysOnTop(true).setOnWindowCloseRequest(windowEvent ->
        {
            lastStateTabBeforeClosing = stateTabPane.getSelectionModel().getSelectedItem();
            lastGlobalTabBeforeClosing = globalTabPane.getSelectionModel().getSelectedItem();

            selectAndMultipleWrite.clear();
            stateSetupPaneList.setupPaneList.forEach(SetupPane::clearAllControlEffect);

            this.onPageClose();
        });

        stateSelectionChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateSelectionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if(newValue != null)
            {
                //Populate all the setup panes with the value of the attributes
                stateSetupPaneList.populateSetupPanes(newValue.getAttributeMap());
                //And update the preview at the bottom of the page when a new state is selected!
                this.updatePreviewImage(newValue);
            }
        });

        createStateMenuItem.setOnAction(actionEvent -> createStateAnchorPane.setVisible(true));
        deleteStateMenuItem.setOnAction(actionEvent ->
        {
            Objects.requireNonNull(selectedControlWrapper, "Trying to delete a WrapperState from the SelectedControlWrapper but is null");

            var wrapperState = stateSelectionChoiceBox.getValue();
            selectedControlWrapper.getStateMap().removeState(wrapperState);

            this.updateStateSelectionStates();
        });

        var stackPane = new StackPane();
        stackPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        stackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        stackPane.setPrefSize(400, 400);

        var tab = new Tab();
        tab.setContent(stackPane);
        globalTabPane.getTabs().add(tab);

        globalTabPane.getStylesheets().add(Util.getResource("stylesheet/tab_pane_header_background.css").toExternalForm());
        stateTabPane.getStylesheets().add(Util.getResource("stylesheet/tab_pane_header_background.css").toExternalForm());

        super.getUndoRedoManager().addCondition(data ->
        {
            if(data instanceof SetupPane<?>)
            {
                stateTabPane.getSelectionModel().select(((SetupPane<?>) data).getTab());
                return true;
            }

            return false;
        });
    }

    @Override
    public void loop()
    {
        if(this.getStageSetter().isShowing())
        {
            var selectedSetupPane = stateSetupPaneList.getSelected();
            if(selectedSetupPane != null)
            {
                var attributeChangerSet = selectedSetupPane.getAttributeChangerList();
                if(attributeChangerSet.isAnyDataChanged())
                {
                    var selectedState = stateSelectionChoiceBox.getValue();
                    attributeChangerSet.setDataToAttribute(selectedState.getAttributeMap(), false);
                    //If any data is changed, update the preview image to be real time ;)
                    this.updatePreviewImage();

                    attributeChangerSet.resetAllDataChanged();
                }
            }

            var selectedGlobalSetupPane = globalSetupPaneList.getSelected();
            if(selectedGlobalSetupPane != null)
            {
                var attributeChangerSet = selectedGlobalSetupPane.getAttributeChangerList();
                if(attributeChangerSet.isAnyDataChanged())
                {
                    var genericAttributeMap = selectedControlWrapper.getGlobalAttributeMap();
                    attributeChangerSet.setDataToAttribute(genericAttributeMap, false);

                    //No need to update preview here, all generics does not change aspect.
                    //But maybe changing attribute, changes the displayed value?
                    //Nope this breaks more than what it solves. Since the address value is updated later, it won't take
                    //effect until the next refresh. NOPE!

                    attributeChangerSet.resetAllDataChanged();
                }
            }
        }
    }

    public ControlContainerPane getControlMainPage()
    {
        return controlContainerPane;
    }

    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    public WrapperState getSelectedWrapperState()
    {
        return stateSelectionChoiceBox.getValue();
    }

    public SelectAndMultipleWrite getSelectAndMultipleWrite()
    {
        return selectAndMultipleWrite;
    }

    public void showStageFor(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null) //It means it was already showing
        {
            this.onPageClose();
        }

        selectedControlWrapper = controlWrapper;

        //Since all the attributes are the same for each state, just compile it for the default state when a new wrapper is being edited
        var stateAttributeTabs = stateTabPane.getTabs();
        stateAttributeTabs.clear();
        stateSetupPaneList.forEachValidTab(selectedControlWrapper.getStateMap().getDefaultState().getAttributeMap(),
                stateAttributeTabs::add);

        var globalAttributeTabs = globalTabPane.getTabs();
        globalAttributeTabs.clear();
        globalSetupPaneList.forEachValidTab(controlWrapper.getGlobalAttributeMap(), globalAttributeTabs::add);

        this.updateStateSelectionStates(); //This also populate state setup panes
        this.populateGenericSetupPanes(); //Generics are populated only one time, not state dependant

        super.getUndoRedoManager().clear(); //Clear all the redo/undo for a new ControlWrapper

        if(lastStateTabBeforeClosing != null)
        {
            stateTabPane.getSelectionModel().select(lastStateTabBeforeClosing);
        }

        if(lastGlobalTabBeforeClosing != null)
        {
            globalTabPane.getSelectionModel().select(lastGlobalTabBeforeClosing);
        }

        super.showStage();
    }

    public void updatePreviewImage()
    {
        this.updatePreviewImage(stateSelectionChoiceBox.getValue());
    }

    private void updatePreviewImage(WrapperState wrapperState)
    {
        if(selectedControlWrapper != null)
        {
            var children = previewStackPane.getChildren();
            children.clear();

            var preview = selectedControlWrapper.createPreviewFor(wrapperState);
            //This is required otherwise the scaling won't work when using adapt
            preview.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            StackPane.setAlignment(preview, Pos.CENTER);

            preview.widthProperty().addListener((observableValue, oldValue, newValue) ->
            {
                var scaleWidth = previewStackPane.getWidth() / newValue.doubleValue();
                if(scaleWidth < 1)
                {
                    preview.setScaleX(scaleWidth);
                }
            });

            preview.heightProperty().addListener((observableValue, oldValue, newValue) ->
            {
                var scaleHeight = previewStackPane.getHeight() / newValue.doubleValue();
                if(scaleHeight < 1)
                {
                    preview.setScaleY(scaleHeight);
                }
            });

            children.add(preview);
        }
    }

    public void updateStateSelectionStates()
    {
        if(selectedControlWrapper != null)
        {
            var choiceBoxItems = stateSelectionChoiceBox.getItems();
            choiceBoxItems.clear();

            var stateMap = selectedControlWrapper.getStateMap();
            stateMap.forEach(choiceBoxItems::add);
            FXCollections.sort(choiceBoxItems);
            //Always select the DefaultState first
            //This automagically also update the preview (Because of the change listener)
            stateSelectionChoiceBox.setValue(stateMap.getDefaultState());
        }
    }

    private void onPageClose()
    {
        if(selectedControlWrapper != null)
        {
            selectedControlWrapper.applyAttributes();
            this.controlContainerPane.getMainEditStage().getQuickPropertiesVBox()
                    .refreshValuesIfSelected(selectedControlWrapper);
            /*
            //Update the current state of the ControlWrapper to avoid update on next data change.
            selectedControlWrapper.getStateMap().getCurrentState().getAttributeMap().setAttributesToControlWrapper();
            selectedControlWrapper.getGlobalAttributeMap().setAttributesToControlWrapper();*/

            selectedControlWrapper = null;
        }
    }

    private void populateGenericSetupPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate GenericSetupPanes without a SelectedControlWrapper");

        var globalAttributeMap = selectedControlWrapper.getGlobalAttributeMap();
        globalSetupPaneList.populateSetupPanes(globalAttributeMap);
    }

    private class SetupPaneList
    {
        private final List<SetupPane<?>> setupPaneList;

        public SetupPaneList()
        {
            setupPaneList = new ArrayList<>();
        }

        public SetupPaneList add(SetupPane<?> setupPane)
        {
            //This is required otherwise when loading stuff could be undo event if it shouldn't
            var undoRedoManager = getControlMainPage().getMainEditStage().getUndoRedoManager();
            undoRedoManager.setIgnoreNew(true);

            setupPane.setup();
            setupPane.setDefault();

            ControlWrapperSetupStage.this.addFXChild(setupPane);

            setupPaneList.add(setupPane);

            undoRedoManager.setIgnoreNew(false);

            return this;
        }

        public SetupPane<?> getSelected()
        {
            for(var setupPane : setupPaneList)
            {
                if(setupPane.getTab().isSelected())
                {
                    return setupPane;
                }
            }

            return null;
        }

        public void forEachValidTab(AttributeMap attributeMap, Consumer<Tab> consumer)
        {
            setupPaneList.stream()
                    .filter(setupPane -> setupPane.hasAttribute(attributeMap))
                    .map(SetupPane::getTab)
                    .forEach(consumer);
        }

        public void populateSetupPanes(AttributeMap attributeMap)
        {
            setupPaneList.stream().map(SetupPane::getAttributeChangerList).forEach(attributeChangerList ->
            {
                attributeChangerList.copyDataFromAttribute(attributeMap);
                //I need to reset all data changes since a lot of stuff might be loaded and change inside the SetupPane
                //and if the "EditAll" button is selected all states are messed up
                attributeChangerList.resetAllDataChanged();
            });
        }
    }
}
