package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.MouseButton;
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
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeInitializer;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeTypeManager;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeUpdater;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.extra.ChangePageExtraFeature;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.extra.ControlWrapperExtraFeature;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.state.WrapperStateMap;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.utils.*;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.util.DoubleClickable;
import parozzz.github.com.simpleplcpanel.hmi.util.Draggable;
import parozzz.github.com.simpleplcpanel.hmi.util.Resizable;
import parozzz.github.com.simpleplcpanel.hmi.util.multipleobjects.DragAndResizeObject;
import parozzz.github.com.simpleplcpanel.hmi.util.specialfunction.FXSpecialFunctionManager;

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

    private final WrapperStateMap stateMap;

    private final AttributeMap globalAttributeMap;
    private final ControlWrapperContextMenuController contextMenuController;
    private final ControlWrapperAttributeTypeManager attributeTypeManager;
    private final ControlWrapperAttributeUpdater<C> attributeUpdater;
    private final DragAndResizeObject dragAndResizeObject;
    private final ControlWrapperSelectionHandler selectionHandler;
    private final ControlWrapperPositionHandler positionHandler;
    private final ControlWrapperUndoRedoHandler undoRedoHandler;

    private final boolean stateless;

    private final ReadOnlyDoubleProperty layoutXReadOnlyProperty;
    private final ReadOnlyDoubleProperty layoutYReadOnlyProperty;

    private final BooleanProperty validProperty;

    private boolean readOnly = false;
    private boolean isDragged;
    private boolean lastPressedWasDrag;
    private boolean lastPressedWasResize;
    private boolean resizing;

    public ControlWrapper(ControlContainerPane controlContainerPane,
            ControlWrapperType<C, ?> wrapperType, boolean stateless)
    {
        this.controlContainerPane = controlContainerPane;
        this.wrapperType = wrapperType;

        this.control = wrapperType.supplyControl();
        this.containerStackPane = new StackPane(control);

        this.addMultipleFXChild(this.stateMap = new WrapperStateMap(this),
                this.globalAttributeMap = new AttributeMap(this, "GenericAttributeMap"),
                this.contextMenuController = new ControlWrapperContextMenuController(this, control, controlContainerPane),
                this.attributeTypeManager = new ControlWrapperAttributeTypeManager(),
                this.attributeUpdater = new ControlWrapperAttributeUpdater<>(this, control),
                this.dragAndResizeObject = new DragAndResizeObject(containerStackPane, controlContainerPane.getMainAnchorPane(), this, controlContainerPane),
                this.selectionHandler = new ControlWrapperSelectionHandler(this),
                this.positionHandler = new ControlWrapperPositionHandler(this),
                this.undoRedoHandler = new ControlWrapperUndoRedoHandler(controlContainerPane, stateMap, globalAttributeMap)
        );

        this.stateless = stateless;

        this.layoutXReadOnlyProperty = ReadOnlyDoubleProperty.readOnlyDoubleProperty(containerStackPane.layoutXProperty());
        this.layoutYReadOnlyProperty = ReadOnlyDoubleProperty.readOnlyDoubleProperty(containerStackPane.layoutYProperty());

        this.validProperty = new SimpleBooleanProperty();
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        serializableDataSet.addInt("LayoutX", containerStackPane.layoutXProperty(), 0)
                .addInt("LayoutY", containerStackPane.layoutYProperty(), 0);

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
            if (width >= 0d) //It can be lower than zero for PRES_SIZE and COMPUTED_SIZE
            {
                var sizeAttribute = globalAttributeMap.getRequired(AttributeType.SIZE);
                sizeAttribute.setValue(SizeAttribute.WIDTH, (int) Math.floor(width));
            }
        });

        containerStackPane.prefHeightProperty().addListener((observable, oldValue, newValue) ->
        {
            var height = newValue.doubleValue();
            if (height >= 0d) //It can be lower than zero for PRES_SIZE and COMPUTED_SIZE
            {
                var sizeAttribute = globalAttributeMap.getRequired(AttributeType.SIZE);
                sizeAttribute.setValue(SizeAttribute.HEIGHT, (int) Math.floor(height));
            }
        });

        specialFunctionManager = FXSpecialFunctionManager
                .builder(containerStackPane, controlContainerPane.getMainAnchorPane())
                .enableDoubleClick(MouseButton.PRIMARY, this,
                        () -> controlContainerPane.getMainEditStage().getControlWrapperSetupStage().setSelectedControlWrapper(this)) //On double click open
                .bind();
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();

        attributeUpdater.updateAllAttributes();
        ControlWrapperBorderCreator.applyDashedBorder(this);
    }

    public double getLayoutX()
    {
        return containerStackPane.getLayoutX();
    }

    public ReadOnlyDoubleProperty layoutXProperty()
    {
        return layoutXReadOnlyProperty;
    }

    public double getLayoutY()
    {
        return containerStackPane.getLayoutY();
    }

    public ReadOnlyDoubleProperty layoutYProperty()
    {
        return layoutYReadOnlyProperty;
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

    public boolean isActive()
    {
        return controlContainerPane.isActive();
    }

    public WrapperStateMap getStateMap()
    {
        return stateMap;
    }

    public AttributeMap getGlobalAttributeMap()
    {
        return globalAttributeMap;
    }

    public ControlWrapperContextMenuController getContextMenuController()
    {
        return contextMenuController;
    }

    public ControlWrapperAttributeTypeManager getAttributeTypeManager()
    {
        return attributeTypeManager;
    }

    public ControlWrapperAttributeUpdater<C> getAttributeUpdater()
    {
        return attributeUpdater;
    }

    public DragAndResizeObject getDragAndResizeObject()
    {
        return dragAndResizeObject;
    }

    public ControlWrapperSelectionHandler getSelectionHandler()
    {
        return selectionHandler;
    }

    public ControlWrapperPositionHandler getPositionHandler()
    {
        return positionHandler;
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

    public void setExtraFeature(ControlWrapperExtraFeature extraFeature)
    {
        if (this.extraFeature != null)
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
        //Only allow resizing for selected controls
        //And if they do not have the adapt attribute on
        return selectionHandler.isSelected()
                && !contextMenuController.isShowing()
                && !globalAttributeMap.getRequired(AttributeType.SIZE).getValue(SizeAttribute.ADAPT);
    }

    @Override
    public final void setIsResizing(boolean resizing)
    {
        this.resizing = resizing;
    }

    @Override
    public final boolean isResizing()
    {
        return resizing;
    }

    @Override
    public final void setLastPressedWasResize(boolean lastPressedWasResize)
    {
        this.lastPressedWasResize = lastPressedWasResize;
    }

    @Override
    public final boolean wasLastPressResize()
    {
        return lastPressedWasResize;
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
    public final boolean wasLastPressDrag()
    {
        return lastPressedWasDrag;
    }

    @Override
    public final boolean canDrag()
    {
        return selectionHandler.isSelected() && !contextMenuController.isShowing();
    }

    @Override
    public final boolean isDragged()
    {
        return isDragged;
    }

    @Override
    public final boolean canDoubleClick()
    {//Allow double click on when an item is actually selected and not dragged :)
        return selectionHandler.isSelected() && !isDragged;
    }

    public final boolean isReadOnly()
    {
        return readOnly;
    }

    public final void convertToReadOnly()
    {
        contextMenuController.remove();
        specialFunctionManager.unbind();
        undoRedoHandler.unregister();
        selectionHandler.removeEventFilters();

        containerStackPane.setBorder(null);

        this.readOnly = true;
    }

    public final void convertToReadWrite()
    {
        contextMenuController.set();
        specialFunctionManager.bind();
        undoRedoHandler.register();
        selectionHandler.addEventFilters();

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
        if (stateless)
        {
            attributeInitializer.addGlobals(AttributeType.BACKGROUND, AttributeType.BORDER);
        } else
        {
            attributeInitializer.addStates(AttributeType.BACKGROUND, AttributeType.BORDER);
        }

        attributeInitializer.addAttributeUpdateConsumer(updateData ->
        {
            var control = updateData.getControl();
            var containerPane = updateData.getContainerPane();

            for (var attributeType : updateData.getAttributeTypeCollection())
            {
                if (!(attributeType == AttributeType.CHANGE_PAGE ||
                        attributeType == AttributeType.SIZE ||
                        attributeType == AttributeType.BACKGROUND ||
                        attributeType == AttributeType.BORDER))
                {
                    continue;
                }

                var attribute = AttributeFetcher.fetch(this, attributeType);
                if (attribute instanceof ChangePageAttribute)
                {
                    if (control == this.control) //This needs to be set only if is the same control as the one inside the controlwrapper
                    {
                        var enabled = attribute.getValue(ChangePageAttribute.ENABLED);
                        if (enabled)
                        {
                            var pageName = attribute.getValue(ChangePageAttribute.PAGE_NAME);
                            this.setExtraFeature(new ChangePageExtraFeature(this, control, pageName));
                        }
                    }
                } else if (attribute instanceof SizeAttribute)
                {
                    var padding = attribute.getValue(SizeAttribute.PADDING);
                    control.setPadding(new Insets(padding));

                    if (attribute.getValue(SizeAttribute.ADAPT))
                    {
                        containerPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                        containerPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                        containerPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                    } else
                    {
                        var width = attribute.getValue(SizeAttribute.WIDTH);
                        var height = attribute.getValue(SizeAttribute.HEIGHT);

                        containerPane.setPrefSize(width, height);
                        containerPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                        containerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                    }
                } else if (attribute instanceof BackgroundAttribute)
                {
                    var backgroundAttribute = (BackgroundAttribute) attribute;
                    control.setBackground(backgroundAttribute.getBackground());
                } else if (attribute instanceof BorderAttribute)
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
                if (addedNode instanceof Text)
                {
                    ((Text) addedNode).setBoundsType(TextBoundsType.VISUAL);
                }
            });
        });

        containerStackPane.setAlignment(Pos.CENTER);
        containerStackPane.setMinSize(10, 10);
        containerStackPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    public abstract ControlWrapper<?> createInstance();

    public ControlWrapper<?> clone()
    {
        var cloneControlWrapper = this.createInstance();
        cloneControlWrapper.onSetup();

        this.stateMap.copyInto(cloneControlWrapper.stateMap);
        this.globalAttributeMap.copyInto(cloneControlWrapper.globalAttributeMap);

        cloneControlWrapper.onSetupComplete();
        return cloneControlWrapper;
    }
}
