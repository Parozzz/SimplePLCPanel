package parozzz.github.com.simpleplcpanel.hmi.redoundo;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;

public class UndoRedoPropertyWrapper<A>
{
    private final UndoRedoManager manager;
    private final UndoRedoPane pane;
    private final Property<A> property;

    private final ChangeListener<A> changeListener;

    private boolean ignoreNextAction;

    public UndoRedoPropertyWrapper(UndoRedoManager manager,  UndoRedoPane pane, Property<A> property)
    {
        this.manager = manager;
        this.pane = pane;
        this.property = property;

        changeListener = (observableValue, oldValue, newValue) ->
        {
            if(!ignoreNextAction)
            {
                manager.addUndoAction(new UndoValue(oldValue));
            }
        };
    }

    void registerListener()
    {
        property.addListener(changeListener);
    }

    void unregisterListener()
    {
        property.removeListener(changeListener);
    }

    public void startIgnoreNextAction()
    {
        ignoreNextAction = true;
    }

    public void stopIgnoreNextAction()
    {
        ignoreNextAction = false;
    }

    public class UndoValue implements UndoAction
    {
        private final A value;
        public UndoValue(A value)
        {
            this.value = value;
        }

        public A getValue()
        {
            return value;
        }

        @Override
        public UndoRedoPane undo(boolean returnPaneOnly)
        {
            if(!returnPaneOnly)
            {
                startIgnoreNextAction();
                property.setValue(value);
                stopIgnoreNextAction();
            }
            return pane;
        }
    }
}
