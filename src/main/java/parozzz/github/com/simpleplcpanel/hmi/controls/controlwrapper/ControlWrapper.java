package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper;

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
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.BackgroundAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.BorderAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.ChangePageAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.SizeAttribute;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeTypeManager;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeUpdater;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.extra.ChangePageExtraFeature;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.extra.ControlWrapperExtraFeature;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperStateMap;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils.ControlWrapperBorderCreator;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils.ControlWrapperContextMenuController;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.util.DoubleClickable;
import parozzz.github.com.simpleplcpanel.hmi.util.Draggable;
import parozzz.github.com.simpleplcpanel.hmi.util.Resizable;
import parozzz.github.com.simpleplcpanel.hmi.util.specialfunction.FXSpecialFunctionManager;
import parozzz.github.com.simpleplcpanel.util.BooleanChangeType;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.Objects;
import java.util.stream.Stream;

public abstract class ControlWrapper<C extends Control>
        extends FXController
        implements Resizable, Draggable, DoubleClickable
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
    private final ControlWrapperAttributeTypeManager attributeTypeManager;
    private final ControlWrapperAttributeUpdater<C> attributeUpdater;

    private final boolean stateless;

    private EventHandler<MouseEvent> mousePressedEventHandler;
    private EventHandler<MouseEvent> mouseReleasedEventHandler;

    private final BooleanProperty validProperty;
    private final BooleanProperty selectedProperty;
    private final BooleanProperty mainSelectionProperty;

    private boolean readOnly = false;
    private boolean isDragged;
    private boolean lastPressedWasDrag;
    private boolean lastPressedWasResize;
    private boolean resizing;

    public ControlWrapper(ControlContainerPane controlContainerPane, ControlWrapperType<C, ?> wrapperType, boolean stateless)
    {
        this.controlContainerPane = controlContainerPane;
        this.wrapperType = wrapperType;

        this.control = wrapperType.supplyControl();
        this.containerStackPane = new StackPane(control);

        this.addFXChild(this.value = wrapperType.createWrapperValue(this, control))
                .addFXChild(this.stateMap = new WrapperStateMap(this))
                .addFXChild(this.globalAttributeMap = new AttributeMap(this, "GenericAttributeMap"))
                .addFXChild(this.contextMenuController = new ControlWrapperContextMenuController(this, control, controlContainerPane))
                .addFXChild(this.attributeTypeManager = new ControlWrapperAttributeTypeManager())
                .addFXChild(this.attributeUpdater = new ControlWrapperAttributeUpdater<>(this, control));

        this.stateless = stateless;

        this.validProperty = new SimpleBooleanProperty();
        this.selectedProperty = new SimpleBooleanProperty();
        this.mainSelectionProperty = new SimpleBooleanProperty();
    }

    @Override
    public void setup()
    {
        super.setup();

        serializableDataSet.addInt("LayoutX", containerStackPane.layoutXProperty())
                .addInt("LayoutY", containerStackPane.layoutYProperty());

        this.setControlVisualProperties(control, containerStackPane);

        var attributeInitializer = new ControlWrapperAttributeInitializer<C>();
        this.registerAttributeInitializers(attributeInitializer);
        attributeTypeManager.initialize(attributeInitializer);
        attributeUpdater.initialize(attributeInitializer);

        globalAttributeMap.parseAttributes(attributeTypeManager, false);
        stateMap.initDefaultState(attributeTypeManager);

        containerStackPane.prefWidthProperty().addListener((observable, oldValue, newValue) ->
        {
            var width = newValue.doubleValue();
            if(width >= 0d) //It can be lower than zero for PRES_SIZE and COMPUTED_SIZE
            {
                var sizeAttribute = globalAttributeMap.get(AttributeType.SIZE);
                Objects.requireNonNull(sizeAttribute, "ControlWrapper has not SizeAttribute?");

                sizeAttribute.setValue(SizeAttribute.WIDTH, (int) Math.floor(width));
            }
        });

        containerStackPane.prefHeightProperty().addListener((observable, oldValue, newValue) ->
        {
            var height = newValue.doubleValue();
            if(height >= 0d) //It can be lower than zero for PRES_SIZE and COMPUTED_SIZE
            {
                var sizeAttribute = globalAttributeMap.get(AttributeType.SIZE);
                Objects.requireNonNull(sizeAttribute, "ControlWrapper has not SizeAttribute?");

                sizeAttribute.setValue(SizeAttribute.HEIGHT, (int) Math.floor(height));
            }
        });

        Stream.of(containerStackPane.widthProperty(), containerStackPane.heightProperty()).forEach(property ->
                property.addListener((observableValue, oldValue, newValue) ->
                { //This update the border (for the corner and center pieces) every time is resized.
                    if(selectedProperty.get())
                    {
                        ControlWrapperBorderCreator.applySelectedBorder(this);
                    }
                })
        );

        containerStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler = mouseEvent ->
        {
            lastPressedWasResize = false; //These should reset on mouse pressed!
            lastPressedWasDrag = false;

            var selectionManager = controlContainerPane.getSelectionManager();
            if(!mouseEvent.isControlDown() && selectionManager.isEmpty())
            { //In this system, if i have control down and there is no selected the first will be added and deleted right away
                selectionManager.set(this);
            }
        }); //No quick properties selection here. Is managed by the selection manager!

        containerStackPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler = mouseEvent ->
        {
            if(lastPressedWasResize || lastPressedWasDrag)
            {
                return;
            }

            var selectionManager = controlContainerPane.getSelectionManager();
            if(mouseEvent.isControlDown())
            {
                if(selectedProperty.get())
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
                .enableResizing(this)
                .bind();

        selectedProperty.addListener((observable, oldValue, newValue) ->
        {
            if(readOnly)
            {
                return;
            }

            switch(Util.checkChangeType(newValue, oldValue))
            {
                case FALLING:
                    ControlWrapperBorderCreator.applyDashedBorder(this);
                    break;
                case RISING:
                    ControlWrapperBorderCreator.applySelectedBorder(this);
                    break;
            }
        });

        mainSelectionProperty.addListener((observable, oldValue, newValue) ->
        {
            if(readOnly)
            {
                return;
            }

            if(Util.checkChangeType(newValue, oldValue) == BooleanChangeType.RISING)
            {
                ControlWrapperBorderCreator.applySelectedBorder(this);
            }
        });
    }

    @Override
    public void setupComplete()
    {
        super.setupComplete();

        attributeUpdater.updateAllAttributes();
        ControlWrapperBorderCreator.applyDashedBorder(this);
    }

    public double getLayoutX()
    {
        return containerStackPane.getLayoutX();
    }

    public double getLayoutY()
    {
        return containerStackPane.getLayoutY();
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

    public ControlWrapperAttributeTypeManager getAttributeTypeManager()
    {
        return attributeTypeManager;
    }

    public ControlWrapperAttributeUpdater<C> getAttributeUpdater()
    {
        return attributeUpdater;
    }

    public boolean isStateless()
    {
        return stateless;
    }

    public boolean isValid()
    {
        return validProperty.get();
    }

    public void setValid(boolean valid)
    {
        validProperty.set(valid);
    }

    public BooleanProperty validProperty()
    {
        return validProperty;
    }

    public boolean isSelected()
    {
        return selectedProperty.get();
    }

    public void setSelected(boolean selected)
    {
        this.selectedProperty.set(selected);
        if(!selected)
        {
            this.mainSelectionProperty.set(false);
        }
    }

    public BooleanProperty selectedProperty()
    {
        return selectedProperty;
    }

    public boolean isMainSelection()
    {
        return mainSelectionProperty.get();
    }

    public void setAsMainSelection()
    {
        this.mainSelectionProperty.set(true);
    }

    public BooleanProperty mainSelectionProperty()
    {
        return mainSelectionProperty;
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
        if(!selectedProperty.get()) //Only allow resizing for selected controls
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
    public final void setLastPressedWasResize(boolean lastPressedWasResize)
    {
        this.lastPressedWasResize = lastPressedWasResize;
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
    public final void setLastPressedWasDrag(boolean lastPressedWasDrag)
    {
        this.lastPressedWasDrag = lastPressedWasDrag;
    }

    @Override
    public final boolean isDragged()
    {
        return isDragged;
    }

    @Override
    public final boolean canDoubleClick()
    {//Allow double click on when an item is actually selected and not dragged :)
        return selectedProperty.get() && !isDragged;
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
        attributeUpdater.setAllAttributesTo(newControl, containerStackPane);
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
        attributeInitializer.addGlobals(AttributeType.CHANGE_PAGE, AttributeType.SIZE);
        if(stateless)
        {
            attributeInitializer.addGlobals(AttributeType.BACKGROUND, AttributeType.BORDER);
        }else
        {
            attributeInitializer.addStates(AttributeType.BACKGROUND, AttributeType.BORDER);
        }

        attributeInitializer.addAttributeUpdateConsumer(updateData ->
        {
            var control = updateData.getControl();
            var containerPane = updateData.getContainerPane();

            for(var attribute : updateData.getAttributeList())
            {
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
                    var padding = attribute.getValue(SizeAttribute.PADDING);
                    control.setPadding(new Insets(padding));

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

    public void copyInto(ControlWrapper<?> pasteControlWrapper)
    {
        this.stateMap.copyInto(pasteControlWrapper.stateMap);
        this.globalAttributeMap.copyInto(pasteControlWrapper.globalAttributeMap);
    }

    private void setControlVisualProperties(C control, StackPane containerStackPane)
    {
        control.setMinSize(10, 10);
        control.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        control.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change ->
        {
            change.next();
            change.getAddedSubList().forEach(addedNode ->
            {
                if(addedNode instanceof Text)
                {
                    ((Text) addedNode).setBoundsType(TextBoundsType.VISUAL);
                }
            });
        });

        containerStackPane.setAlignment(Pos.CENTER);
        containerStackPane.setMinSize(10, 10);
        containerStackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }
}
