package parozzz.github.com.hmi.redoundo;

import javafx.beans.property.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class UndoRedoManager
{
    private final List<RunnableManager> undoRunnableList;
    private final List<RunnableManager> redoRunnableList;

    private final List<Predicate<Object>> conditionList;

    private boolean ignoreNew;
    public UndoRedoManager()
    {
        this.undoRunnableList = new ArrayList<>();
        this.redoRunnableList = new ArrayList<>();

        this.conditionList = new ArrayList<>();
    }

    public void addCondition(Predicate<Object> predicate)
    {
        conditionList.add(predicate);
    }

    public void setIgnoreNew(boolean ignoreNew)
    {
        this.ignoreNew = ignoreNew;
    }

    public void addAction(Runnable undoRunnable, Runnable redoRunnable, Object data)
    {
        undoRunnableList.add(0, new RunnableManager(undoRunnable, redoRunnable, data));
        if(undoRunnableList.size() > 20) //Max 20 undo actions
        {
            undoRunnableList.remove(20);
        }
    }

    public void addProperties(Collection<Property<?>> propertyCollection)
    {
        this.addProperties(propertyCollection, null);
    }

    public void addProperties(Collection<Property<?>> propertyCollection, Object data)
    {
        propertyCollection.forEach(property -> this.addProperty(property, data));
    }

    public <T> UndoRedoManager addProperty(Property<T> property)
    {
        return addProperty(property, null);
    }

    public <T> UndoRedoManager addProperty(Property<T> property, Object data)
    {
        property.addListener((observableValue, oldValue, newValue) ->
        {
            if (ignoreNew)
            {
                return;
            }

            Runnable undoRunnable = () -> property.setValue(oldValue);
            Runnable redoRunnable = () -> property.setValue(newValue);
            this.addAction(undoRunnable, redoRunnable, data);
        });

        return this;
    }

    public void undo()
    {
        if(undoRunnableList.isEmpty())
        {
            return;
        }

        var runnableManager = undoRunnableList.remove(0);
        Objects.requireNonNull(runnableManager, "Found a null RunnableManager for UndoRedo class while undoing");

        if(!conditionList.stream().allMatch(predicate -> predicate.test(runnableManager.data)))
        {
            return;
        }

        ignoreNew = true;
        runnableManager.undoRunnable.run();
        ignoreNew = false;

        redoRunnableList.add(0, runnableManager);
        if(redoRunnableList.size() > 20) //Max 20 redo actions
        {
            redoRunnableList.remove(20);
        }
    }

    public void redo()
    {
        if(redoRunnableList.isEmpty())
        {
            return;
        }

        var runnableManager = redoRunnableList.remove(0);
        Objects.requireNonNull(runnableManager, "Found a null RunnableManager for UndoRedo class while redoing");

        ignoreNew = true;
        runnableManager.redoRunnable.run();
        ignoreNew = false;
    }

    public void clear()
    {
        undoRunnableList.clear();
        redoRunnableList.clear();
    }

    private static class RunnableManager
    {
        private final Runnable undoRunnable;
        private final Runnable redoRunnable;
        private final Object data;
        private RunnableManager(Runnable undoRunnable, Runnable redoRunnable, Object data)
        {
            this.undoRunnable = undoRunnable;
            this.redoRunnable = redoRunnable;
            this.data = data;
        }
    }

}
