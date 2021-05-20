package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress.SiemensS7StringAddressCreator;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperGenericAttributeUpdateConsumer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.*;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.ButtonDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.control.InputDataSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.BorderPaneHMIStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public final class ControlWrapperSetupStage
        extends BorderPaneHMIStage
        implements ControlWrapperSpecific
{
    @FXML private Button createStateButton;

    @FXML private VBox attributesVBox;

    @FXML private VBox globalAttributesVBox;
    @FXML private TitledPane globalAttributesTitledPane;

    @FXML private VBox stateAttributesVBox;
    @FXML private TitledPane stateAttributesTitledPane;

    @FXML private Label selectedPageLabel;
    @FXML private StackPane centerStackPane;

    @FXML private Label stateLabel;
    @FXML private ChoiceBox<WrapperState> stateSelectionChoiceBox;
    @FXML private Button deleteStateButton;

    private final MainEditStage mainEditStage;
    private final TagsManager tagsManager;
    private final CommunicationDataHolder communicationDataHolder;

    private final SetupPaneList setupPaneList;

    private final WrapperStateCreationPane wrapperStateCreationPane;

    private final ChangeListener<Boolean> controlWrapperValidListener;
    private final ControlWrapperGenericAttributeUpdateConsumer attributesUpdatedConsumer;

    private ControlWrapper<?> selectedControlWrapper;
    private SetupSelectable activeSelectable;
    private boolean ignoreAttributeChanges;

    public ControlWrapperSetupStage(MainEditStage mainEditStage,
            TagsManager tagsManager, CommunicationDataHolder communicationDataHolder) throws IOException
    {
        super("ControlWrapperSetupPage", "setup/mainSetupPane.fxml");

        this.mainEditStage = mainEditStage;
        this.tagsManager = tagsManager;
        this.communicationDataHolder = communicationDataHolder;

        this.setupPaneList = new SetupPaneList();

        this.addFXChild(wrapperStateCreationPane = new WrapperStateCreationPane(this, createStateButton));

        controlWrapperValidListener = (observableValue, oldValue, newValue) ->
        {
            if (!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        attributesUpdatedConsumer = updateData ->
        {
            if (selectedControlWrapper == null)
            {
                throw new IllegalStateException("Listening for attribute update but the selected control wrapper is null!");
            }

            var selectedWrapperState = stateSelectionChoiceBox.getValue();
            if (selectedWrapperState == null)
            {
                return;
            }

            ignoreAttributeChanges = true;
            for (var attribute : updateData.getAttributeList())
            {
                var attributeType = attribute.getType();
                setupPaneList.populateOf(selectedControlWrapper, selectedWrapperState, attributeType);
            }
            ignoreAttributeChanges = false;
        };
    }

    @Override
    public void setup()
    {
        super.setup();

        try
        {
            setupPaneList.add(new SizeSetupPane(this)).//This first >:)
                    add(new ButtonDataSetupPane(this)). //And this second! >:(
                    add(new InputDataSetupPane(this)).
                    add(new ChangePageSetupPane(this)).
                    add(new FontSetupPane(this)).
                    add(new TextSetupPane(this)).
                    add(new BorderSetupPane(this)).
                    add(new BackgroundSetupPane(this)).
                    add(new ValueSetupPane(this)).
                    add(new AddressSetupPane<>(this, tagsManager, communicationDataHolder,"Write Address", AttributeType.WRITE_ADDRESS)).
                    add(new AddressSetupPane<>(this, tagsManager, communicationDataHolder,"Read Address", AttributeType.READ_ADDRESS)); //I want this last! >:(
        } catch (IOException exception)
        {
            MainLogger.getInstance().error("Error while loading Setup Panes", exception, this);
        }

        this.getStageSetter().setAlwaysOnTop(true)
                .setResizable(true)
                .setWidth(615)
                .setHeight(615) //To avoid starting extra small
                .setOnWindowCloseRequest(windowEvent -> this.setSelectedControlWrapper(null));

        ContextMenuBuilder.builder()
                .simple("Write to All", this::writeEntireCurrentStateToAll)
                .setTo(stateAttributesTitledPane);

        stateSelectionChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateSelectionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
        {
            this.populateStateSetupPanes();
            if (selectedControlWrapper != null)
            {
                selectedControlWrapper.getStateMap().forceCurrentState(newValue);
            }
        });

        deleteStateButton.setOnAction(event ->
        {
            if (selectedControlWrapper != null)
            {
                selectedControlWrapper.getStateMap().removeState(stateSelectionChoiceBox.getValue());
                this.updateStateSelectionBox();
            }
        });

        super.getUndoRedoManager().addCondition(data ->
        {
            if (data instanceof SetupSelectable)
            {
                this.setShownSelectable((SetupSelectable) data);
                return true;
            }

            return false;
        });
    }

    @Override
    public void loop()
    {
        if (!(this.getStageSetter().isShowing() && activeSelectable instanceof SetupPane<?>))
        {
            return;
        }

        if (selectedControlWrapper == null)
        {
            return;
        }

        var setupPane = (SetupPane<?>) activeSelectable;

        var attributeChangerSet = setupPane.getAttributeChangerList();
        if (attributeChangerSet.isAnyDataChanged())
        {
            if (ignoreAttributeChanges)
            {
                return;
            }

            var attributeType = setupPane.getAttributeType();
            var attributeManager = selectedControlWrapper.getAttributeTypeManager();
            if (attributeManager.isState(attributeType))
            {
                var selectedState = stateSelectionChoiceBox.getValue();
                attributeChangerSet.saveDataToAttribute(selectedState.getAttributeMap(), false);
            } else if (attributeManager.isGlobal(attributeType))
            {
                var globalAttributeMap = selectedControlWrapper.getGlobalAttributeMap();
                attributeChangerSet.saveDataToAttribute(globalAttributeMap, false);
            }

            //this.updatePreviewImage(); //If any data is changed, update the preview image to be real time ;)
            attributeChangerSet.resetAllDataChanged();
        }
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if (selectedControlWrapper != null) //It means it was already showing
        {
            mainEditStage.getQuickPropertiesVBox().refreshValuesIfSelected(selectedControlWrapper);

            selectedControlWrapper.validProperty().removeListener(controlWrapperValidListener);
            selectedControlWrapper.getAttributeUpdater().removeGenericUpdateConsumer(attributesUpdatedConsumer);

            selectedControlWrapper = null;
        }

        selectedControlWrapper = controlWrapper;
        if (controlWrapper == null)
        {
            setupPaneList.forEach(SetupPane::clearControlWrapper);

            this.hideStage();
            return;
        }

        //If a control wrapper is stateless, i will disable all stuff state related!
        var stateless = controlWrapper.isStateless();
        stateLabel.setDisable(stateless);
        stateSelectionChoiceBox.setDisable(stateless);
        deleteStateButton.setDisable(stateless);

        controlWrapper.validProperty().addListener(controlWrapperValidListener);
        controlWrapper.getAttributeUpdater().addGenericUpdateConsumer(attributesUpdatedConsumer);

        this.populateButtonPanes();
        this.updateStateSelectionBox(); //This also populate state setup panes
        //State Setup Panes are populated automagically by the listener of the state choicebox
        this.populateGlobalSetupPanes(); //Generics are populated only one time, not state dependant

        super.getUndoRedoManager().clear(); //Clear all the redo/undo for a new ControlWrapper

        if (activeSelectable instanceof SetupPane<?> &&
                !AttributeFetcher.hasAttribute(
                        selectedControlWrapper, ((SetupPane<?>) activeSelectable).getAttributeType()
                ))
        {
            this.setShownSelectable(null);
        }

        super.showStage();
    }

    @Override
    public ControlWrapper<?> getSelectedControlWrapper()
    {
        return selectedControlWrapper;
    }

    public WrapperState getSelectedWrapperState()
    {
        return stateSelectionChoiceBox.getValue();
    }

    public void setShownSelectable(SetupSelectable selectable)
    {
        var children = centerStackPane.getChildren();
        children.clear();

        if (this.activeSelectable != null)
        {
            var oldSelectButton = this.activeSelectable.getSelectButton();
            oldSelectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
            selectedPageLabel.setText("");
        }

        if (selectable != null)
        {
            children.add(selectable.getParent());

            var newSelectButton = selectable.getSelectButton();
            newSelectButton.setBackground(FXUtil.createBackground(Color.LIMEGREEN));
            selectedPageLabel.setText(newSelectButton.getText());

            if (selectedControlWrapper != null && selectable instanceof SetupPane<?>)
            {
                var attributeType = ((SetupPane<?>) selectable).getAttributeType();
                var attributeManager = selectedControlWrapper.getAttributeTypeManager();
                if (attributeManager.isState(attributeType))
                {
                    stateAttributesTitledPane.setExpanded(true);
                } else if (attributeManager.isGlobal(attributeType))
                {
                    globalAttributesTitledPane.setExpanded(true);
                }

            }
        }

        this.activeSelectable = selectable;
    }

    public void updateStateSelectionBox()
    {
        if (selectedControlWrapper != null)
        {
            var stateMap = selectedControlWrapper.getStateMap();

            var choiceBoxItems = stateSelectionChoiceBox.getItems();
            choiceBoxItems.clear();
            stateMap.forEach(choiceBoxItems::add);
            FXCollections.sort(choiceBoxItems);

            //Always select the DefaultState first
            //This automagically also update the preview (Because of the change listener)
            stateSelectionChoiceBox.setValue(stateMap.getDefaultState());
        }
    }

    private void populateButtonPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate button panes with a selected control wrapper");

        //This also removed both the titled pane (That will be added in case the controlwrapper is not stateless)
        var attributesVBoxChildren = attributesVBox.getChildren();
        attributesVBoxChildren.clear();

        //Since a control wrapper can be stateless, i want to avoid titled pane and go all buttons just there ;)
        if (selectedControlWrapper.isStateless())
        {
            setupPaneList.forEach(setupPane ->
            { //Doing this way is should respect the order given inside the list
                var attributeType = setupPane.getAttributeType();
                var attributeManager = selectedControlWrapper.getAttributeTypeManager();
                if (attributeManager.isGlobal(attributeType))
                {
                    attributesVBoxChildren.add(setupPane.getSelectButton());
                }
            });
        } else
        {
            //In case it has states, i just add back both the titles pane and the create state button!
            attributesVBoxChildren.addAll(createStateButton,
                    globalAttributesTitledPane, stateAttributesTitledPane);

            //Since all the attributes are the same for each state, just compile it for the default state when a new wrapper is being edited
            var stateAttributeButtonsChildren = stateAttributesVBox.getChildren();
            var globalAttributeButtonsChildren = globalAttributesVBox.getChildren();

            Stream.of(stateAttributeButtonsChildren, globalAttributeButtonsChildren).forEach(List::clear);

            setupPaneList.forEach(setupPane ->
            { //Doing this way is should respect the order given inside the list
                var attributeType = setupPane.getAttributeType();
                var attributeManager = selectedControlWrapper.getAttributeTypeManager();

                if (attributeManager.isState(attributeType))
                {
                    stateAttributeButtonsChildren.add(setupPane.getSelectButton());
                } else if (attributeManager.isGlobal(attributeType))
                {
                    globalAttributeButtonsChildren.add(setupPane.getSelectButton());
                }
            });
        }
    }

    private void populateStateSetupPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate StateSetupPanes without a SelectedControlWrapper");

        var wrapperState = stateSelectionChoiceBox.getValue();
        if (wrapperState != null)
        {
            selectedControlWrapper.getAttributeTypeManager().forEachState(attributeType ->
            {
                var setupPane = setupPaneList.getFromAttributeType(attributeType);
                setupPaneList.populate(setupPane, wrapperState.getAttributeMap());

                setupPane.setAsState();
            });
            //this.updatePreviewImage(wrapperState); //And update the preview at the bottom of the page when a new state is selected!
        }
    }

    private void populateGlobalSetupPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate GlobalSetupPanes without a SelectedControlWrapper");
        selectedControlWrapper.getAttributeTypeManager().forEachGlobal(attributeType ->
        {
            var setupPane = setupPaneList.getFromAttributeType(attributeType);
            setupPaneList.populate(setupPane, selectedControlWrapper.getGlobalAttributeMap());

            setupPane.setAsGlobal();
        });
    }

    private void writeEntireCurrentStateToAll()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot write current state without a SelectedControlWrapper");

        selectedControlWrapper.getAttributeTypeManager().forEachState(attributeType ->
                setupPaneList.getFromAttributeType(attributeType).writeToAllStates()
        );
    }

    private class SetupPaneList implements Iterable<SetupPane<?>>
    {
        private final List<SetupPane<?>> setupPaneList;
        private final Map<AttributeType<?>, SetupPane<?>> attributeTypeSetupPaneMap;

        public SetupPaneList()
        {
            this.setupPaneList = new ArrayList<>();
            this.attributeTypeSetupPaneMap = new HashMap<>();
        }

        public SetupPaneList add(SetupPane<?> setupPane)
        {
            //This is required otherwise when loading stuff could be undo event if it shouldn't
            var undoRedoManager = mainEditStage.getUndoRedoManager();
            undoRedoManager.setIgnoreNew(true);

            try
            {
                setupPane.setup();
                setupPane.setDefault();

                ControlWrapperSetupStage.this.addFXChild(setupPane);

                setupPaneList.add(setupPane);
                attributeTypeSetupPaneMap.put(setupPane.getAttributeType(), setupPane);
            } catch (Exception exception)
            {
                MainLogger.getInstance().warning("Error while adding a SetupPane", exception);
            }

            undoRedoManager.setIgnoreNew(false);

            return this;
        }

        public SetupPane<?> getFromAttributeType(AttributeType<?> attributeType)
        {
            return Objects.requireNonNull(attributeTypeSetupPaneMap.get(attributeType),
                    "A SetupPane for attribute " + attributeType.toString() + " does not exists"
            );
        }

        public boolean contains(Object object)
        {
            return setupPaneList.contains(object);
        }

        public void populateOf(ControlWrapper<?> controlWrapper, WrapperState wrapperState,
                AttributeType<?> attributeType)
        {
            var setupPane = this.getFromAttributeType(attributeType);

            var attributeManager = controlWrapper.getAttributeTypeManager();
            if (attributeManager.isState(attributeType))
            {
                this.populate(setupPane, wrapperState.getAttributeMap());
            } else if (attributeManager.isGlobal(attributeType))
            {
                this.populate(setupPane, controlWrapper.getGlobalAttributeMap());
            }
        }

        private void populate(SetupPane<?> setupPane, AttributeMap attributeMap)
        {
            var attributeChangerList = setupPane.getAttributeChangerList();
            attributeChangerList.readDataFromAttribute(attributeMap);
            //I need to reset all data changes since a lot of stuff might be loaded and change inside the SetupPane
            //and if the "EditAll" button is selected all states are messed up
            attributeChangerList.resetAllDataChanged();
        }

        @Override
        public Iterator<SetupPane<?>> iterator()
        {
            return setupPaneList.iterator();
        }
    }
}



    /*
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
*/