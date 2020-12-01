package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeMap;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.hmi.attribute.impl.BorderAttribute;
import parozzz.github.com.hmi.attribute.impl.ChangePageAttribute;
import parozzz.github.com.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeManager;
import parozzz.github.com.hmi.controls.controlwrapper.extra.ChangePageExtraFeature;
import parozzz.github.com.hmi.controls.controlwrapper.extra.ControlWrapperExtraFeature;
import parozzz.github.com.hmi.controls.controlwrapper.others.ControlWrapperBorderCreator;
import parozzz.github.com.hmi.controls.controlwrapper.others.ControlWrapperContextMenuController;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperStateMap;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.hmi.util.DoubleClickable;
import parozzz.github.com.hmi.util.Draggable;
import parozzz.github.com.hmi.util.Resizable;
import parozzz.github.com.hmi.util.specialfunction.FXSpecialFunctionManager;
import parozzz.github.com.util.TrigBoolean;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class ControlWrapper<C extends Control> extends FXController implements Resizable, Draggable, DoubleClickable
{
    public static final String VALUE_PLACEHOLDER = "{value}";

    protected FXSpecialFunctionManager specialFunctionManager;
    protected ControlWrapperExtraFeature extraFeature;

    private final ControlContainerPane controlContainerPane;
    private final ControlWrapperType<C, ?> wrapperType;

    protected final C control;
    private final StackPane containerStackPane;

    private final ControlWrapperValue<C> value;
    private final WrapperStateMap stateMap;

    private final AttributeMap globalAttributeMap; //This will stay here, but is still for a future. WIP
    private final ControlWrapperContextMenuController contextMenuController;
    private final ControlWrapperAttributeManager<C> attributeManager;

    private final TrigBoolean selected;
    private final TrigBoolean mainSelection;

    private EventHandler<MouseEvent> mousePressedEventHandler;
    private EventHandler<MouseEvent> mouseReleasedEventHandler;

    private final BooleanProperty validProperty;
    private boolean readOnly = false;
    private boolean isDragged;
    private boolean resizing;

    public ControlWrapper(ControlContainerPane controlContainerPane, ControlWrapperType<C, ?> wrapperType,
            BiFunction<ControlWrapper<C>, C, ControlWrapperValue<C>> valueSupplierCreator)
    {
        super("ControlWrapper_" + controlContainerPane.getNextControlWrapperIdentifier());

        this.controlContainerPane = controlContainerPane;
        this.wrapperType = wrapperType;

        this.control = wrapperType.supplyControl();
        this.containerStackPane = new StackPane(control);

        this.addFXChild(this.value = valueSupplierCreator.apply(this, control))
                .addFXChild(this.stateMap = new WrapperStateMap(this))
                .addFXChild(this.globalAttributeMap = new AttributeMap(this, "GenericAttributeMap"))
                .addFXChild(this.contextMenuController = new ControlWrapperContextMenuController(this, control, controlContainerPane))
                .addFXChild(this.attributeManager = new ControlWrapperAttributeManager<>(this, control));

        this.selected = new TrigBoolean(false);
        this.mainSelection = new TrigBoolean(false);

        this.validProperty = new SimpleBooleanProperty();
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addInt("LayoutX", containerStackPane.layoutXProperty())
                .addInt("LayoutY", containerStackPane.layoutYProperty());

        this.setControlVisualProperties(control, containerStackPane);

        var attributeInitializer = new ControlWrapperAttributeInitializer<>(this);
        this.registerAttributeInitializers(attributeInitializer);
        attributeManager.initialize(attributeInitializer);

        globalAttributeMap.parseAttributes(attributeManager, false);
        stateMap.initDefaultState(attributeManager);

        /*
        var stateAttributeList = new ArrayList<Attribute>();
        var globalAttributeList = new ArrayList<Attribute>();

        this.registerAttributeInitializers(stateAttributeList, globalAttributeList);
        //Initialize the consumer inside the DefaultState for the control of this wrapper
        stateMap.initDefaultState(wrapperState ->
                stateAttributeList.forEach(wrapperState.getAttributeMap()::addAttribute));

        globalAttributeList.forEach(globalAttributeMap::addAttribute);
*/
        Stream.of(containerStackPane.widthProperty(), containerStackPane.heightProperty()).forEach(property ->
                property.addListener((observableValue, oldValue, newValue) ->
                {
                    if(selected.get())
                    {
                        ControlWrapperBorderCreator.applySelectedBorder(this);
                    }
                })
        );

        containerStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler = mouseEvent ->
        {
            var selectionManager = controlContainerPane.getSelectionManager();
            //In this system, if i have control down and there is no selected the first will be added and deleted right away
            if(!mouseEvent.isControlDown() && selectionManager.isEmpty())
            {
                selectionManager.set(this);
            }

            //No quick properties selection here. Is managed by the selection manager!
            //controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelected(this);
        });

        containerStackPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler = mouseEvent ->
        {
            if(isDragged)
            {
                return;
            }

            var selectionManager = controlContainerPane.getSelectionManager();
            if(mouseEvent.isControlDown())
            {
                if(selected.get())
                {
                    selectionManager.remove(this);
                }else
                {
                    selectionManager.add(this);
                }
            }else
            {
                selectionManager.set(this);
            }
        });

        specialFunctionManager = FXSpecialFunctionManager
                .builder(containerStackPane, controlContainerPane.getMainAnchorPane())
                .enableDoubleClick(MouseButton.PRIMARY, this,
                        () -> controlContainerPane.getMainEditStage().getControlWrapperSetupStage().setSelectedControlWrapper(this)) //On double click open
                .enableResizing(this, width ->
                {
                    stateMap.forEach(wrapperState ->
                    {
                        var attribute = AttributeFetcher.fetch(this, AttributeType.SIZE);
                        if(attribute != null)
                        {
                            attribute.setValue(SizeAttribute.WIDTH, (int) Math.floor(width));
                        }
                    });
                }, height ->
                {
                    stateMap.forEach(wrapperState ->
                    {
                        var attribute = AttributeFetcher.fetch(this, AttributeType.SIZE);
                        if(attribute != null)
                        {
                            attribute.setValue(SizeAttribute.HEIGHT, (int) Math.floor(height));
                        }
                    });
                }).bind();
    }

    @Override
    public void loop()
    {
        super.loop();

        if(!readOnly)
        {
            var trigType = selected.checkTrig();
            switch(trigType)
            {
                case FALLING:
                    ControlWrapperBorderCreator.applyDashedBorder(this);
                    break;
                case RISING:
                    ControlWrapperBorderCreator.applySelectedBorder(this);
                    break;
            }

            if(mainSelection.checkTrig() == TrigBoolean.TrigType.RISING)
            {
                ControlWrapperBorderCreator.applySelectedBorder(this);
            }
        }
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        attributeManager.updateAllAttributes();
        ControlWrapperBorderCreator.applyDashedBorder(this);
    }

    public ControlWrapperType<C, ?> getType()
    {
        return wrapperType;
    }

    public ControlContainerPane getControlMainPage()
    {
        return controlContainerPane;
    }

    public Pane getContainerPane()
    {
        return containerStackPane;
    }

    public ControlWrapperValue<C> getValue()
    {
        return value;
    }

    public WrapperStateMap getStateMap()
    {
        return stateMap;
    }

    public AttributeMap getGlobalAttributeMap()
    {
        return globalAttributeMap;
    }

    public ControlWrapperAttributeManager<C> getAttributeManager()
    {
        return attributeManager;
    }

    public BooleanProperty validProperty()
    {
        return validProperty;
    }

    public boolean isSelected()
    {
        return selected.get();
    }

    public void setSelected(boolean selected)
    {
        this.selected.set(selected);
        if(!selected)
        {
            this.mainSelection.set(false);
        }
    }

    public void setAsMainSelection()
    {
        this.mainSelection.set(true);
    }

    public boolean isMainSelection()
    {
        return mainSelection.get();
    }

    public void setExtraFeature(ControlWrapperExtraFeature extraFeature)
    {
        if(this.extraFeature != null)
        {
            this.extraFeature.unbind();
        }

        this.extraFeature = extraFeature;
        extraFeature.bind();
    }

    public ControlWrapperExtraFeature getExtraFeature()
    {
        return extraFeature;
    }

    @Override
    public final boolean canResize()
    {
        if(!selected.get()) //Only allow resizing for selected controls
        {
            return false;
        }
        //And if they do not have the adapt attribute on
        var sizeAttribute = AttributeFetcher.fetch(this, AttributeType.SIZE);
        return !(sizeAttribute == null || sizeAttribute.getValue(SizeAttribute.ADAPT));
    }

    @Override
    public final void setResizing(boolean resizing)
    {
        this.resizing = resizing;
    }

    @Override
    public final boolean isResizing()
    {
        return resizing;
    }

    @Override
    public final void setIsDragged(boolean isDragged)
    {
        this.isDragged = isDragged;
    }

    @Override
    public final boolean isDragged()
    {
        return isDragged;
    }

    @Override
    public final boolean canDoubleClick()
    {//Allow double click on when an item is actually selected and not dragged :)
        return selected.get() && !isDragged;
    }

    public final void convertToReadOnly()
    {
        contextMenuController.remove();
        specialFunctionManager.unbind();

        containerStackPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        containerStackPane.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        containerStackPane.setBorder(null);

        this.readOnly = true;
    }

    public final void convertToReadWrite()
    {
        contextMenuController.set();
        specialFunctionManager.bind();

        containerStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        containerStackPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        ControlWrapperBorderCreator.applyDashedBorder(this);

        this.readOnly = false;
    }

    public Region createPreviewFor(WrapperState wrapperState)
    {
        var newControl = wrapperType.supplyControl();

        var containerStackPane = new StackPane(newControl);
        this.setControlVisualProperties(newControl, containerStackPane);
        attributeManager.setAllAttributesTo(newControl, containerStackPane);
        return containerStackPane;
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonData = super.serialize();
        //This is only for identification inside the JSON when reading manually
        jsonData.set("WrapperType", wrapperType.getName());

        return jsonData;
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);
    }

    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<C> attributeInitializer)
    {
        //GLOBAL
        attributeInitializer.addGlobals(AttributeType.CHANGE_PAGE)
                .addStates(AttributeType.SIZE, AttributeType.BACKGROUND, AttributeType.BORDER)
                .addAttributeUpdateConsumer(updateData ->
                {
                    var control = updateData.getControl();
                    var containerPane = updateData.getContainerPane();

                    for(var attributeType : updateData.getAttributeTypeList())
                    {
                        var attribute = AttributeFetcher.fetch(this, attributeType);
                        if(attribute == null)
                        {
                            continue;
                        }

                        if(attribute instanceof ChangePageAttribute)
                        {
                            if(control == this.control) //This needs to be set only if is the same control as the one inside the controlwrapper
                            {
                                var enabled = attribute.getValue(ChangePageAttribute.ENABLED);
                                if(enabled)
                                {
                                    var pageName = attribute.getValue(ChangePageAttribute.PAGE_NAME);
                                    this.setExtraFeature(new ChangePageExtraFeature(this, control, pageName));
                                }
                            }
                        }else if(attribute instanceof SizeAttribute)
                        {
                            if(attribute.getValue(SizeAttribute.ADAPT))
                            {
                                containerPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                                containerPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                                containerPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                            }else
                            {
                                var width = attribute.getValue(SizeAttribute.WIDTH);
                                var height = attribute.getValue(SizeAttribute.HEIGHT);

                                containerPane.setPrefSize(width, height);
                                containerPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                                containerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                            }
                        }else if(attribute instanceof BackgroundAttribute)
                        {
                            var backgroundAttribute = (BackgroundAttribute) attribute;
                            control.setBackground(backgroundAttribute.getBackground());
                        }else if(attribute instanceof BorderAttribute)
                        {
                            var borderAttribute = (BorderAttribute) attribute;
                            control.setBorder(borderAttribute.getBorder());
                        }
                    }
                });
    }

    /*
    protected void registerAttributeInitializers(List<Attribute> stateAttributeList,
            List<Attribute> globalAttributeList)
    {
        //GLOBALS
        globalAttributeList.add(new ChangePageAttribute(this));

        //STATE SPECIFIC
        var pictureBank = this.getControlMainPage().getMainEditStage().getPictureBankStage();
        stateAttributeList.add(new SizeAttribute(this));
        stateAttributeList.add(new BackgroundAttribute(this, pictureBank));
        stateAttributeList.add(new BorderAttribute(this));
    }

    private <A extends Attribute> void registerAttributeConsumer(Class<A> attributeClass, Consumer<A> consumer)
    {
        attributeUpdateConsumerMap.put(attributeClass, consumer);
    }

    public void applyAttributes(Object involvedObject)
    {
        this.applyAttributes(stateMap.getCurrentState().getAttributeMap(), involvedObject);
    }

    public void applyAttributes(AttributeMap attributeMap, Object involvedObject)
    {
        this.applyAttributes(control, containerStackPane, attributeMap, involvedObject);
    }

    public void applyAttributes(C control, Pane containerPane, AttributeMap attributeMap, Object involvedObject)
    {
        if (involvedObject != null)
        {
            attributeUpdatedRunnableSet.forEach(consumer -> consumer.accept(involvedObject));
        }

        var sizeAttribute = AttributeFetcher.fetch(attributeMap, SizeAttribute.class);
        if (sizeAttribute != null)
        {
            if (sizeAttribute.getValue(SizeAttribute.ADAPT))
            {
                containerPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                containerPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                containerPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            } else
            {
                var width = sizeAttribute.getValue(SizeAttribute.WIDTH);
                var height = sizeAttribute.getValue(SizeAttribute.HEIGHT);

                containerPane.setPrefSize(width, height);
                containerPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                containerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }
        }

        var backgroundAttribute = AttributeFetcher.fetch(attributeMap, BackgroundAttribute.class);
        if (backgroundAttribute != null)
        {
            control.setBackground(backgroundAttribute.getBackground());
        }

        var borderAttribute = AttributeFetcher.fetch(attributeMap, BorderAttribute.class);
        if (borderAttribute != null)
        {
            control.setBorder(borderAttribute.getBorder());
        }

        if (control == this.control) //This needs to be set only
        {
            var changePageAttribute = AttributeFetcher.fetch(globalAttributeMap, ChangePageAttribute.class);
            if (changePageAttribute != null)
            {
                var enabled = changePageAttribute.getValue(ChangePageAttribute.ENABLED);
                if (enabled)
                {
                    var pageName = changePageAttribute.getValue(ChangePageAttribute.PAGE_NAME);
                    this.setExtraFeature(new ChangePageExtraFeature(this, control, pageName));
                }
            }
        }
    }
*/
    /*
    @Override
    public ControlWrapper<C> clone()
    {
        var cloneControlWrapper = wrapperType.createWrapper(controlContainerPane);
        stateMap.forEachNoDefault(wrapperState ->
        {
            var cloneWrapperState = wrapperState.clone(cloneControlWrapper);
            cloneControlWrapper.stateMap.addState(cloneWrapperState, false);
        });

        var pasteDefaultState = cloneControlWrapper.stateMap.getDefaultState();
        stateMap.getDefaultState().copyInto(pasteDefaultState);
        return cloneControlWrapper;
    }
*/
    public void copyInto(ControlWrapper<?> pasteControlWrapper)
    {
        this.stateMap.copyInto(pasteControlWrapper.stateMap);
        this.globalAttributeMap.copyInto(pasteControlWrapper.globalAttributeMap);
    }

    /*
    public void cloneInto(ControlWrapper<?> cloneControlWrapper)
    {
        this.stateMap.forEachNoDefault(wrapperState ->
                cloneControlWrapper.getStateMap().addState(wrapperState.clone(), false)
        );

        var defaultState = this.stateMap.getDefaultState();
        var cloneDefaultState = cloneControlWrapper.getStateMap().getDefaultState();
        cloneDefaultState.getAttributeMap().cloneFromOther(defaultState.getAttributeMap());
    }
*/
    private void setControlVisualProperties(C control, StackPane containerStackPane)
    {
        control.setMinSize(10, 10);
        control.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        //The paddings are for the wrapping text
        control.setPadding(new Insets(-2));
        control.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change ->
        {
            change.next();
            change.getAddedSubList().forEach(addedNode ->
            {
                if(addedNode instanceof Text)
                {
                    var text = (Text) addedNode;
                    text.setBoundsType(TextBoundsType.VISUAL);
                }
            });
        });

        containerStackPane.setAlignment(Pos.CENTER);
        containerStackPane.setMinSize(10, 10);
        containerStackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }
}
