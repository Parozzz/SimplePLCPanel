package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperSpecific;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeUpdateConsumer;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperGenericAttributeUpdateConsumer;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.*;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control.ButtonDataSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.control.InputDataSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.page.BorderPaneHMIStage;
import parozzz.github.com.hmi.util.ContextMenuBuilder;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.logger.MainLogger;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ControlWrapperSetupStage extends BorderPaneHMIStage implements ControlWrapperSpecific
{
    @FXML
    private Button createStateButton;

    @FXML
    private VBox globalAttributesVBox;
    @FXML
    private TitledPane globalAttributesTitledPane;

    @FXML
    private VBox stateAttributesVBox;
    @FXML
    private TitledPane stateAttributesTitledPane;

    @FXML
    private Label selectedPageLabel;
    @FXML
    private StackPane centerStackPane;

    @FXML
    private ChoiceBox<WrapperState> stateSelectionChoiceBox;
    @FXML
    private Button deleteStateButton;

    private final MainEditStage mainEditStage;

    private final SetupPaneList stateSetupPaneList;
    private final SetupPaneList globalSetupPaneList;

    private final ChangeListener<Boolean> controlWrapperValidListener;
    private final ControlWrapperGenericAttributeUpdateConsumer attributesUpdatedConsumer;

    private ControlWrapper<?> selectedControlWrapper;
    private SetupSelectable activeSelectable;
    private boolean ignoreAttributeChanges;

    public ControlWrapperSetupStage(MainEditStage mainEditStage) throws IOException
    {
        super("ControlWrapperSetupPage", "setup/mainSetupPane.fxml");

        this.mainEditStage = mainEditStage;

        this.stateSetupPaneList = new SetupPaneList();
        this.globalSetupPaneList = new SetupPaneList();

        this.addFXChild(new WrapperStateCreationPane(this, createStateButton));

        controlWrapperValidListener = (observableValue, oldValue, newValue) ->
        {
            if(!newValue)
            {
                this.setSelectedControlWrapper(null);
            }
        };
        attributesUpdatedConsumer = updateData ->
        {
            if(selectedControlWrapper == null)
            {
                throw new IllegalStateException("Listening for attribute update but the selected control wrapper is null!");
            }

            var selectedWrapperState = stateSelectionChoiceBox.getValue();
            if(selectedWrapperState == null)
            {
                return;
            }

            ignoreAttributeChanges = true;
            for(var attributeType : updateData.getAttributeTypeList())
            {
                stateSetupPaneList.populateOf(selectedWrapperState.getAttributeMap(), attributeType);
                globalSetupPaneList.populateOf(selectedControlWrapper.getGlobalAttributeMap(), attributeType);
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
            stateSetupPaneList.add(new SizeSetupPane(this)).
                    add(new FontSetupPane(this)).
                    add(new TextSetupPane(this)).
                    add(new BorderSetupPane(this)).
                    add(new BackgroundSetupPane(this)).
                    add(new ValueSetupPane(this)).
                    add(new AddressSetupPane<>(this, "Write Address", AttributeType.WRITE_ADDRESS, false));

            globalSetupPaneList.add(new ButtonDataSetupPane(this)) //I want this first! >:)
                    .add(new InputDataSetupPane(this))
                    .add(new ChangePageSetupPane(this))
                    .add(new AddressSetupPane<>(this, "Read Address", AttributeType.READ_ADDRESS, true)); //I want this last! >:(
        }
        catch(IOException exception)
        {
            Logger.getLogger(ControlWrapperSetupStage.class.getSimpleName())
                    .log(Level.WARNING, "Exception while loading ControlWrapperSetupPage", exception);
        }

        this.getStageSetter().setAlwaysOnTop(true)
                .setResizable(true)
                .setWidth(600)
                .setHeight(550) //To avoid starting extra small
                .setOnWindowCloseRequest(windowEvent -> this.setSelectedControlWrapper(null));

        ContextMenuBuilder.builder()
                .simple("Write to All", this::writeEntireCurrentStateToAll)
                .setTo(stateAttributesTitledPane);

        stateSelectionChoiceBox.setConverter(FXUtil.toStringOnlyConverter(WrapperState::getStringVersion));
        stateSelectionChoiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) ->
                this.populateStateSetupPanes()
        );

        deleteStateButton.setOnAction(event ->
        {
            if(selectedControlWrapper != null)
            {
                selectedControlWrapper.getStateMap().removeState(stateSelectionChoiceBox.getValue());
                this.updateStateSelectionBox();
            }
        });

        super.getUndoRedoManager().addCondition(data ->
        {
            if(data instanceof SetupSelectable)
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
        if(!(this.getStageSetter().isShowing() && activeSelectable instanceof SetupPane<?>))
        {
            return;
        }

        var setupPane = (SetupPane<?>) activeSelectable;

        var attributeChangerSet = setupPane.getAttributeChangerList();
        if(attributeChangerSet.isAnyDataChanged())
        {
            if(ignoreAttributeChanges)
            {
                return;
            }

            if(stateSetupPaneList.contains(setupPane))
            {
                var selectedState = stateSelectionChoiceBox.getValue();
                attributeChangerSet.setDataToAttribute(selectedState.getAttributeMap(), false);
                //If any data is changed, update the preview image to be real time ;)
                //this.updatePreviewImage();

                attributeChangerSet.resetAllDataChanged();
            }else if(globalSetupPaneList.contains(setupPane))
            {
                var globalAttributeMap = selectedControlWrapper.getGlobalAttributeMap();
                attributeChangerSet.setDataToAttribute(globalAttributeMap, false);

                //No need to update preview here, all globals does not change aspect.
                //But maybe changing attribute, changes the displayed value?
                //Nope this breaks more than what it solves. Since the address value is updated later, it won't take
                //effect until the next refresh. NOPE!

                attributeChangerSet.resetAllDataChanged();
            }
        }
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    @Override
    public void setSelectedControlWrapper(ControlWrapper<?> controlWrapper)
    {
        if(selectedControlWrapper != null) //It means it was already showing
        {
            mainEditStage.getQuickPropertiesVBox().refreshValuesIfSelected(selectedControlWrapper);

            selectedControlWrapper.validProperty().removeListener(controlWrapperValidListener);
            selectedControlWrapper.getAttributeManager().removeGenericUpdateConsumer(attributesUpdatedConsumer);
            /*
            //Update the current state of the ControlWrapper to avoid update on next data change.
            selectedControlWrapper.getStateMap().getCurrentState().getAttributeMap().setAttributesToControlWrapper();
            selectedControlWrapper.getGlobalAttributeMap().setAttributesToControlWrapper();*/
            selectedControlWrapper = null;
        }

        selectedControlWrapper = controlWrapper;
        if(controlWrapper == null)
        {
            super.getStageSetter().close();
            return;
        }

        controlWrapper.validProperty().addListener(controlWrapperValidListener);
        controlWrapper.getAttributeManager().addGenericUpdateConsumer(attributesUpdatedConsumer);

        //Since all the attributes are the same for each state, just compile it for the default state when a new wrapper is being edited
        var stateAttributeButtonsChildren = stateAttributesVBox.getChildren();
        stateAttributeButtonsChildren.clear();
        stateSetupPaneList.forEachValidButton(selectedControlWrapper.getStateMap().getDefaultState().getAttributeMap(),
                stateAttributeButtonsChildren::add);

        var globalAttributeButtonsChildren = globalAttributesVBox.getChildren();
        globalAttributeButtonsChildren.clear();
        globalSetupPaneList.forEachValidButton(controlWrapper.getGlobalAttributeMap(), globalAttributeButtonsChildren::add);

        this.updateStateSelectionBox(); //This also populate state setup panes
        this.populateGlobalSetupPanes(); //Generics are populated only one time, not state dependant

        super.getUndoRedoManager().clear(); //Clear all the redo/undo for a new ControlWrapper

        if(activeSelectable instanceof SetupPane<?> &&
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

        if(this.activeSelectable != null)
        {
            var oldSelectButton = this.activeSelectable.getSelectButton();
            oldSelectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
            selectedPageLabel.setText("");
        }

        if(selectable != null)
        {
            children.add(selectable.getParent());

            var newSelectButton = selectable.getSelectButton();
            newSelectButton.setBackground(FXUtil.createBackground(Color.LIMEGREEN));
            selectedPageLabel.setText(newSelectButton.getText());

            if(stateSetupPaneList.contains(selectable))
            {
                stateAttributesTitledPane.setExpanded(true);
            }else if(globalSetupPaneList.contains(selectable))
            {
                globalAttributesTitledPane.setExpanded(true);
            }
        }

        this.activeSelectable = selectable;
    }

    /*public void updatePreviewImage()
    {
        this.updatePreviewImage(stateSelectionChoiceBox.getValue());
    }*/

    /*
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
    public void updateStateSelectionBox()
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

    private void populateStateSetupPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate StateSetupPanes without a SelectedControlWrapper");

        var wrapperState = stateSelectionChoiceBox.getValue();
        if(wrapperState != null)
        {
            stateSetupPaneList.populateAll(wrapperState.getAttributeMap());
            //And update the preview at the bottom of the page when a new state is selected!
            //this.updatePreviewImage(wrapperState);
        }
    }

    private void populateGlobalSetupPanes()
    {
        Objects.requireNonNull(selectedControlWrapper, "Cannot populate GlobalSetupPanes without a SelectedControlWrapper");

        var globalAttributeMap = selectedControlWrapper.getGlobalAttributeMap();
        globalSetupPaneList.populateAll(globalAttributeMap);
    }

    private void writeEntireCurrentStateToAll()
    {
        for(var setupPane : stateSetupPaneList)
        {
            setupPane.writeToAllStates();
        }
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
            }
            catch(Exception exception)
            {
                MainLogger.getInstance().warning("Error while adding a SetupPane", exception);
            }

            undoRedoManager.setIgnoreNew(false);

            return this;
        }

        public SetupPane<?> getFromAttributeType(AttributeType<?> attributeType)
        {
            return attributeTypeSetupPaneMap.get(attributeType);
        }

        public boolean contains(Object object)
        {
            return setupPaneList.contains(object);
        }

        public void forEachValidButton(AttributeMap attributeMap, Consumer<Button> consumer)
        {
            setupPaneList.stream()
                    .filter(setupPane -> setupPane.hasAttribute(attributeMap))
                    .map(SetupPane::getSelectButton)
                    .forEach(consumer);
        }

        public void populateOf(AttributeMap attributeMap, AttributeType<?> attributeType)
        {
            var setupPane = attributeTypeSetupPaneMap.get(attributeType);
            if(setupPane != null)
            {
                this.populate(setupPane, attributeMap);
            }
        }

        public void populateAll(AttributeMap attributeMap)
        {
            setupPaneList.forEach(setupPane -> this.populate(setupPane, attributeMap));
        }

        private void populate(SetupPane<?> setupPane, AttributeMap attributeMap)
        {
            var attributeChangerList = setupPane.getAttributeChangerList();
            attributeChangerList.copyDataFromAttribute(attributeMap);
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
