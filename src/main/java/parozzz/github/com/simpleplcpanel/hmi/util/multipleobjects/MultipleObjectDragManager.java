package parozzz.github.com.simpleplcpanel.hmi.util.multipleobjects;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import parozzz.github.com.simpleplcpanel.hmi.util.Draggable;
import parozzz.github.com.simpleplcpanel.hmi.util.Resizable;
import parozzz.github.com.simpleplcpanel.util.MathUtil;

import java.util.HashSet;
import java.util.Set;

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
        var alreadyExists = objectDragSet.stream().anyMatch(objectDrag -> objectDrag.draggable == region);
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
            if(objectDrag.draggable == region)
            {
                objectDrag.removeEventHandlers();
                return true;
            }

            return false;
        });
    }

    public ObjectDrag getObjectDragOfController(Object controller)
    {
        for(var objectDrag : objectDragSet)
        {
            if(objectDrag.controller == controller)
            {
                return objectDrag;
            }
        }

        return null;
    }

    public class ObjectDrag
    {
        private final Region draggable;
        private final Object controller;

        private final EventHandler<MouseEvent> mousePressedEventHandler;
        private final EventHandler<MouseEvent> dragEventHandler;
        private final EventHandler<MouseEvent> mouseReleaseEventHandler;

        private double startX;
        private double startY;

        private ObjectDrag(Region draggable, Object controller)
        {
            this.draggable = draggable;
            this.controller = controller;

            mousePressedEventHandler = mouseEvent ->
            {
                if(controller instanceof Draggable)
                {
                    var tDraggable = (Draggable) controller;
                    tDraggable.setLastPressedWasDrag(false);
                }

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
                    var tDraggable = (Draggable) controller;
                    tDraggable.setIsDragged(true);
                    tDraggable.setLastPressedWasDrag(true);
                }

                var xDiff = mouseEvent.getX() - startX;
                var yDiff = mouseEvent.getY() - startY;
                this.move(xDiff, yDiff);
            };

            mouseReleaseEventHandler = mouseEvent ->
            {
                if(controller instanceof Draggable)
                {
                    ((Draggable) controller).setIsDragged(false);
                }
            };
        }

        public void move(double xDiff, double yDiff)
        {
            var newLayoutX = draggable.getLayoutX() + xDiff;
            newLayoutX = Math.max(0, newLayoutX);
            newLayoutX = Math.min(regionContainer.getWidth() - draggable.getWidth(), newLayoutX);

            var newLayoutY = draggable.getLayoutY() + yDiff;
            newLayoutY = Math.max(0, newLayoutY);
            newLayoutY = Math.min(regionContainer.getHeight() - draggable.getHeight(), newLayoutY);

            xDiff = newLayoutX - draggable.getLayoutX();
            var minXDiff = xDiff;

            yDiff = newLayoutY - draggable.getLayoutY();
            var minYDiff = yDiff;

            for(var otherObjectDrag : objectDragSet)
            {
                if(otherObjectDrag == this)
                {
                    continue;
                }

                var otherDraggable = otherObjectDrag.draggable;

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
                var draggable = objectDrag.draggable;
                draggable.setLayoutX(draggable.getLayoutX() + minXDiff);
                draggable.setLayoutY(draggable.getLayoutY() + minYDiff);
            }
        }

        private void addEventHandlers()
        {
            draggable.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            draggable.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
            draggable.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        }

        private void removeEventHandlers()
        {
            draggable.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            draggable.removeEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
            draggable.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        }

        private boolean isControllerResizing()
        {
            return controller instanceof Resizable && ((Resizable) controller).isResizing();
        }
    }
}
