package parozzz.github.com.simpleplcpanel.hmi.redoundo;

import javafx.beans.property.Property;

import java.util.*;

public class UndoRedoManager
{
    private static UndoRedoManager INSTANCE;

    public static UndoRedoManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new UndoRedoManager();
        }

        return INSTANCE;
    }

    public static final int MAX_SAVED_REDO_ACTIONS = 50;

    private final List<UndoAction> undoActionList;
    private final Map<Property<?>, UndoRedoPropertyWrapper<?>> propertyWrapperMap;

    private boolean ignoreNextActions;

    private UndoRedoManager()
    {
        this.undoActionList = new ArrayList<>();
        //this.redoRunnableList = new ArrayList<>();

        this.propertyWrapperMap = new IdentityHashMap<>();
    }

    public void startIgnoringNextActions()
    {
        ignoreNextActions = true;
    }

    public void stopIgnoringNextActions()
    {
        ignoreNextActions = false;
    }

    public <N> void registerProperty(UndoRedoPane pane, Property<N> property)
    {
        var propertyWrapper = new UndoRedoPropertyWrapper<>(this, pane, property);
        if(propertyWrapperMap.putIfAbsent(property, propertyWrapper) == null)
        {
            propertyWrapper.registerListener();
        }
    }

    public void unregisterProperty(Property<?> property)
    {
        var propertyWrapper = propertyWrapperMap.remove(property);
        if(propertyWrapper != null)
        {
            propertyWrapper.unregisterListener();
        }
    }

    public <N> void registerProperties(UndoRedoPane pane, Property<N>... properties)
    {
        for (var property : properties)
        {
            this.registerProperty(pane, property);
        }
    }

    public void removeAllUndoActionsWithPane(UndoRedoPane pane)
    {
        undoActionList.removeIf(action -> action.undo(true) == pane);
    }

    public void addUndoAction(UndoAction action)
    {
        if (!ignoreNextActions)
        {
            undoActionList.add(0, action);
            if (undoActionList.size() > MAX_SAVED_REDO_ACTIONS)
            {
                undoActionList.remove(undoActionList.size() - 1); //Remove the first one (LIFO)
            }
        }
    }

    public void undo()
    {
        if (undoActionList.isEmpty())
        {
            return;
        }

        var undoAction = undoActionList.remove(0);
        Objects.requireNonNull(undoAction, "Found a null UndoAction for UndoRedo class while undoing");
/*
        if(!conditionList.stream().allMatch(predicate -> predicate.test(undoAction.data)))
        {
            return;
        }
*/
        startIgnoringNextActions();
        var pane = undoAction.undo(false);
        if (pane != null)
        {
            pane.undoActionExecuted();
        }
        stopIgnoringNextActions();

        //this.addToList(redoRunnableList, undoAction);
    }

    /*
        public void redo()
        {
            if(redoRunnableList.isEmpty())
            {
                return;
            }

            var runnableManager = redoRunnableList.remove(0);
            Objects.requireNonNull(runnableManager, "Found a null RunnableManager for UndoRedo class while redoing");

            ignoreNextActions = true;
            runnableManager.redoRunnable.run();
            ignoreNextActions = false;

            this.addToList(undoActionList, runnableManager);
        }
    */
    public void clear()
    {
        undoActionList.clear();
        //redoRunnableList.clear();
    }
}
