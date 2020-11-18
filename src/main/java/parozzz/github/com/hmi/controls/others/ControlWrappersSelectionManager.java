package parozzz.github.com.hmi.controls.others;

import com.sun.javafx.collections.TrackableObservableList;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.util.multipleobjects.MultipleObjectDragManager;

import java.util.function.Consumer;

public final class ControlWrappersSelectionManager extends FXObject
{
    private final ControlContainerPane controlContainerPane;

    private final MultipleObjectDragManager dragManager;
    private final ObservableList<ControlWrapper<?>> selectedControlWrapperList;

    private ControlWrapper<?> mainSelection;

    public ControlWrappersSelectionManager(ControlContainerPane controlContainerPane, Region regionContainer)
    {
        super("SelectedControlWrappersManager");

        this.controlContainerPane = controlContainerPane;

        this.dragManager = new MultipleObjectDragManager(regionContainer);
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
                        addedControlWrapper.setSelected(true);
                        dragManager.addRegion(addedControlWrapper.getContainerPane(), addedControlWrapper);
                    }

                    for(var removedControlWrapper : change.getRemoved())
                    {
                        removedControlWrapper.setSelected(false);
                        dragManager.removeRegion(removedControlWrapper.getContainerPane());
                    }
                }

                if(mainSelection != null && !mainSelection.isSelected())
                {
                    mainSelection = null;
                    controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelected(null);
                }

                if(mainSelection == null && finalList.size() != 0)
                {
                    (mainSelection = finalList.get(0)).setAsMainSelection();
                    controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelected(mainSelection);
                }
            }
        });
    }

    @Override
    public void setup()
    {
        super.setup();

        //Mouse pressed is better here otherwise when releasing the mouse outside one it will clear selection
        controlContainerPane.getMainAnchorPane().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            var pickResult = mouseEvent.getPickResult();
            //If the clicked part is the actual pane and not something else
            if(pickResult.getIntersectedNode() == mouseEvent.getSource())
            {
                this.selectedControlWrapperList.clear(); //Remove all selected!

                //Also clear the quick properties!
                controlContainerPane.getMainEditStage().getQuickPropertiesVBox().setSelected(null);
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
    {
        if(mainSelection != null)
        {//This way should take care of moving them all and checking the bounds
            var objectDrag = dragManager.getObjectDragOfController(mainSelection);
            if(objectDrag != null)
            {
                objectDrag.move(xDiff, yDiff);
            }
        }

        /*
        for(var controlWrapper : selectedControlWrapperList)
        {
            var containerPane = controlWrapper.getContainerPane();
            containerPane.setLayoutX(containerPane.getLayoutX() + xDiff);
            containerPane.setLayoutY(containerPane.getLayoutY() + yDiff);
        }*/
    }
}
