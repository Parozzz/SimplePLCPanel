package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.utils;

import javafx.collections.ListChangeListener;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperStateMap;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoPane;

public final class ControlWrapperUndoRedoHandler extends FXObject
{

    private final UndoRedoPane undoRedoPane;
    private final WrapperStateMap wrapperStateMap;
    private final AttributeMap globalAttributeMap;

    private final ListChangeListener<WrapperState> wrapperStateListChangeListener;

    public ControlWrapperUndoRedoHandler(UndoRedoPane undoRedoPane,
            WrapperStateMap wrapperStateMap, AttributeMap globalAttributeMap)
    {
        this.undoRedoPane = undoRedoPane;
        this.wrapperStateMap = wrapperStateMap;
        this.globalAttributeMap = globalAttributeMap;

        this.wrapperStateListChangeListener = change -> {
            while (change.next())
            {
                change.getAddedSubList().forEach(wrapperState ->
                        registerAttributeMap(undoRedoPane, wrapperState.getAttributeMap())
                );

                change.getRemoved().forEach(wrapperState ->
                        unregisterAttributeMap(wrapperState.getAttributeMap())
                );
            }
        };
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        this.register();
    }

    public void register()
    {
        registerAttributeMap(undoRedoPane, globalAttributeMap);

        wrapperStateMap.forEach(wrapperState ->
                registerAttributeMap(undoRedoPane, wrapperState.getAttributeMap())
        );

        wrapperStateMap.wrapperStateListProperty().addListener(wrapperStateListChangeListener);
    }

    public void unregister()
    {
        unregisterAttributeMap(globalAttributeMap);

        wrapperStateMap.forEach(wrapperState ->
                unregisterAttributeMap(wrapperState.getAttributeMap())
        );

        wrapperStateMap.wrapperStateListProperty().removeListener(wrapperStateListChangeListener);
    }

    private void registerAttributeMap(UndoRedoPane undoRedoPane, AttributeMap attributeMap)
    {
        attributeMap.forEach(attribute ->
                attribute.forEachProperty(property ->
                        UndoRedoManager.getInstance().registerProperty(undoRedoPane, property)
                )
        );
    }

    private void unregisterAttributeMap(AttributeMap attributeMap)
    {
        attributeMap.forEach(attribute ->
                attribute.forEachProperty(UndoRedoManager.getInstance()::unregisterProperty)
        );
    }
}
