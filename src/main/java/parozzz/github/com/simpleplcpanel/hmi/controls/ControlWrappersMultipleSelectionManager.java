package parozzz.github.com.simpleplcpanel.hmi.controls;

import com.sun.javafx.collections.TrackableObservableList;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.util.multipleobjects.DragAndResizeObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class ControlWrappersMultipleSelectionManager extends FXObject
{
    private final ControlContainerPane controlContainerPane;

    private final List<DragAndResizeObject> dragAndResizeObjectList;
    private final ObservableList<ControlWrapper<?>> selectedControlWrapperList;

    private ControlWrapper<?> mainSelection;

    public ControlWrappersMultipleSelectionManager(ControlContainerPane controlContainerPane, Region regionContainer)
    {
        this.controlContainerPane = controlContainerPane;

        this.dragAndResizeObjectList = new ArrayList<>();
        this.selectedControlWrapperList = new SimpleListProperty<>(new TrackableObservableList<>()
        {
            @Override
            protected void onChanged(ListChangeListener.Change<ControlWrapper<?>> change)
            {
                var finalList = change.getList();
                while(change.next())
                {
                    for(var addedControlWrapper : change.getAddedSubList())
                    {
                        addedControlWrapper.getSelectionHandler().setSelected(true);

                        var dragAndResizeObject = addedControlWrapper.getDragAndResizeObject();
                        dragAndResizeObjectList.add(dragAndResizeObject);

                        dragAndResizeObject.setGroupedObjectIterable(dragAndResizeObjectList);
                    }

                    for(var removedControlWrapper : change.getRemoved())
                    {
                        removedControlWrapper.getSelectionHandler().setSelected(false);

                        var dragAndResizeObject = removedControlWrapper.getDragAndResizeObject();
                        dragAndResizeObjectList.remove(dragAndResizeObject);

                        dragAndResizeObject.setGroupedObjectIterable(null);
                    }
                }

                if(mainSelection != null && !mainSelection.getSelectionHandler().isSelected())
                {
                    mainSelection = null;
                    controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelectedControlWrapper(null);
                }

                if(mainSelection == null && finalList.size() != 0)
                {
                    (mainSelection = finalList.get(0)).getSelectionHandler().setMainSelection(true);
                    controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelectedControlWrapper(mainSelection);
                }
            }
        });
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        //Mouse pressed is better here otherwise when releasing the mouse outside one it will clear selection
        controlContainerPane.getMainAnchorPane().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            var pickResult = mouseEvent.getPickResult();
            //If the clicked part is the actual pane and not something else
            if(pickResult.getIntersectedNode() == mouseEvent.getSource())
            {
                this.selectedControlWrapperList.clear(); //Remove all selected!

                //Also clear the quick properties!
                controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelectedControlWrapper(null);
            }
        });
    }

    public ControlWrapper<?> getMainSelection()
    {
        return mainSelection;
    }

    public void remove(ControlWrapper<?> controlWrapper)
    {
        selectedControlWrapperList.remove(controlWrapper);
    }

    public void set(ControlWrapper<?> controlWrapper)
    {
        selectedControlWrapperList.clear();
        this.add(controlWrapper);
    }

    public void add(ControlWrapper<?> controlWrapper)
    {
        selectedControlWrapperList.add(controlWrapper);
    }

    public void forEach(Consumer<ControlWrapper<?>> consumer)
    {
        selectedControlWrapperList.forEach(consumer);
    }

    public void forEachIgnoring(ControlWrapper<?> toBeIgnored, Consumer<ControlWrapper<?>> consumer)
    {
        for(var controlWrapper : selectedControlWrapperList)
        {
            if(controlWrapper != toBeIgnored)
            {
                consumer.accept(controlWrapper);
            }
        }
    }

    public int size()
    {
        return selectedControlWrapperList.size();
    }

    public boolean isEmpty()
    {
        return selectedControlWrapperList.isEmpty();
    }

    public void deleteAll()
    {
        for(var controlWrapper : selectedControlWrapperList)
        {
            controlContainerPane.deleteControlWrapper(controlWrapper);
        }

        selectedControlWrapperList.clear();
    }

    public void clearSelections()
    {
        selectedControlWrapperList.clear();
    }

    public void moveAll(double xDiff, double yDiff)
    {//This will move all the objects inside the collections above!
        if(dragAndResizeObjectList.size() >= 1)
        {
            dragAndResizeObjectList.get(0).move(xDiff, yDiff, true);
        }
    }

    public void resizeAll(double newWidth, double newHeight)
    {//This will resize all the objects inside the collections above!
        if(dragAndResizeObjectList.size() >= 1)
        {
            dragAndResizeObjectList.get(0).resize(newWidth, newHeight, true);
        }
    }
}
