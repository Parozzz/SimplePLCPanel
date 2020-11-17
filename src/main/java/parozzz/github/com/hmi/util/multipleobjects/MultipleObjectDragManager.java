package parozzz.github.com.hmi.util.multipleobjects;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import parozzz.github.com.hmi.util.Draggable;
import parozzz.github.com.hmi.util.Resizable;
import parozzz.github.com.util.MathUtil;

import java.util.*;

public final class MultipleObjectDragManager
{
    private final Region regionContainer;
    private final Set<ObjectDrag> objectDragSet;

    public MultipleObjectDragManager(Region regionContainer)
    {
        this.regionContainer = regionContainer;
        this.objectDragSet = new HashSet<>();
    }

    public void addRegion(Region region)
    {
        this.addRegion(region, null);
    }

    public void addRegion(Region region, Object controller)
    {
        var alreadyExists = objectDragSet.stream().anyMatch(objectDrag -> objectDrag.region == region);
        if(!alreadyExists)
        {
            var objectDrag = new ObjectDrag(region, controller);
            objectDrag.addEventHandlers();
            objectDragSet.add(objectDrag);
        }
    }

    public void removeRegion(Region region)
    {
        objectDragSet.removeIf(objectDrag ->
        {
            if(objectDrag.region == region)
            {
                objectDrag.removeEventHandlers();
                return true;
            }

            return false;
        });
    }

    private class ObjectDrag
    {
        private final Region region;
        private final Object controller;

        private final EventHandler<MouseEvent> mousePressedEventHandler;
        private final EventHandler<MouseEvent> dragEventHandler;
        private final EventHandler<MouseEvent> mouseReleaseEventHandler;

        private double startX;
        private double startY;

        private ObjectDrag(Region region, Object controller)
        {
            this.region = region;
            this.controller = controller;

            mousePressedEventHandler = mouseEvent ->
            {
                if(mouseEvent.getButton() != MouseButton.PRIMARY
                        || this.isControllerResizing())
                {
                    startX = -1;
                    startY = -1;
                    return;
                }

                startX = mouseEvent.getX();
                startY = mouseEvent.getY();
            };

            dragEventHandler = mouseEvent ->
            {
                if(startX == -1 || startY == -1 ||
                        mouseEvent.getButton() != MouseButton.PRIMARY)
                {
                    return;
                }

                if(controller instanceof Draggable)
                {
                    ((Draggable) controller).setIsDragged(true);
                }

                var source = mouseEvent.getSource();
                if(!(source instanceof Region))
                {
                    return;
                }

                var draggable = (Region) source;

                var newLayoutX = draggable.getLayoutX() + (mouseEvent.getX() - startX);
                newLayoutX = Math.max(0, newLayoutX);
                newLayoutX = Math.min(regionContainer.getWidth() - draggable.getWidth(), newLayoutX);

                var newLayoutY = draggable.getLayoutY() + (mouseEvent.getY() - startY);
                newLayoutY = Math.max(0, newLayoutY);
                newLayoutY = Math.min(regionContainer.getHeight() - draggable.getHeight(), newLayoutY);

                var xDiff = newLayoutX - draggable.getLayoutX();
                double minXDiff = xDiff;

                var yDiff = newLayoutY - draggable.getLayoutY();
                var minYDiff = yDiff;

                for(var otherObjectDrag : objectDragSet)
                {
                    if(otherObjectDrag == this)
                    {
                        continue;
                    }

                    var otherDraggable = otherObjectDrag.region;

                    if(xDiff != 0)
                    {
                        var otherNewLayoutX = otherDraggable.getLayoutX() + xDiff;
                        otherNewLayoutX = Math.max(0, otherNewLayoutX);
                        otherNewLayoutX = Math.min(regionContainer.getWidth() - otherDraggable.getWidth(), otherNewLayoutX);

                        var otherXDiff = otherNewLayoutX - otherDraggable.getLayoutX();
                        minXDiff = MathUtil.findNearestToZero(otherXDiff, minXDiff);//Math.min(otherXDiff, minXDiff);
                    }

                    if(yDiff != 0)
                    {
                        var otherNewLayoutY = otherDraggable.getLayoutY() + yDiff;
                        otherNewLayoutY = Math.max(0, otherNewLayoutY);
                        otherNewLayoutY = Math.min(regionContainer.getHeight() - otherDraggable.getHeight(), otherNewLayoutY);

                        var otherYDiff = otherNewLayoutY - otherDraggable.getLayoutY();
                        minYDiff = MathUtil.findNearestToZero(otherYDiff, minYDiff);//Math.min(otherYDiff, minYDiff);
                    }
                }

                for(var objectDrag : objectDragSet)
                {
                    var objectDragRegion = objectDrag.region;
                    objectDragRegion.setLayoutX(objectDragRegion.getLayoutX() + minXDiff);
                    objectDragRegion.setLayoutY(objectDragRegion.getLayoutY() + minYDiff);
                }
            };

            mouseReleaseEventHandler = mouseEvent ->
            {
                if(controller instanceof Draggable)
                {
                    ((Draggable) controller).setIsDragged(false);
                }
            };
        }

        public void addEventHandlers()
        {
            region.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            region.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
            region.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        }

        public void removeEventHandlers()
        {
            region.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            region.removeEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
            region.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        }

        private boolean isControllerResizing()
        {
            return controller instanceof Resizable && ((Resizable) controller).isResizing();
        }
    }
}
