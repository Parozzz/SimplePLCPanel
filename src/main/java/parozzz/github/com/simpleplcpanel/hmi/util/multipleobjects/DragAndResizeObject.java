package parozzz.github.com.simpleplcpanel.hmi.util.multipleobjects;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Region;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.simpleplcpanel.hmi.redoundo.UndoRedoPane;
import parozzz.github.com.simpleplcpanel.hmi.util.Draggable;
import parozzz.github.com.simpleplcpanel.hmi.util.Resizable;
import parozzz.github.com.simpleplcpanel.util.MathUtil;
import parozzz.github.com.simpleplcpanel.util.ReflectionUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.lang.reflect.Field;

public class DragAndResizeObject extends FXObject
{
    private static final Field BORDER_STROKE_OUTER_EDGE = ReflectionUtil.getField(BorderStroke.class, "outerEdge");

    protected final Region targetRegion;
    protected final Region containerRegion;
    @Nullable private final Resizable resizable;
    @Nullable private final Draggable draggable;
    private final UndoRedoPane undoRedoPane;

    private final EventHandler<MouseEvent> containerMouseAnyEvent;
    private final EventHandler<MouseEvent> mousePressedEventHandler;
    private final EventHandler<MouseEvent> dragEventHandler;
    private final EventHandler<MouseEvent> mouseReleaseEventHandler;
    private final EventHandler<MouseEvent> mouseMovedEventHandler;
    private final EventHandler<MouseEvent> mouseExitEventHandler;

    // ==== BOTH ====
    private double startX = -1;
    private double startY = -1;
    // ==== BOTH ====

    // ==== RESIZABLE ====
    private double startWidth;
    private double startHeight;
    private double startLayoutX;
    private double startLayoutY;
    private double resizeXMultiplier;
    private double resizeYMultiplier;
    private boolean mouseOnEdge;
    // ==== RESIZABLE ====

    @Nullable private Iterable<? extends DragAndResizeObject> groupedObjectIterable;
    private double containerMouseX = -1;
    private double containerMouseY = -1;

    public DragAndResizeObject(Region targetRegion, Region containerRegion, Object controller,
            UndoRedoPane undoRedoPane)
    {
        this.targetRegion = targetRegion;
        this.containerRegion = containerRegion;
        this.resizable = controller instanceof Resizable ? (Resizable) controller : null;
        this.draggable = controller instanceof Draggable ? (Draggable) controller : null;
        this.undoRedoPane = undoRedoPane;

        containerMouseAnyEvent = event ->
        {
            containerMouseX = event.getX();
            containerMouseY = event.getY();
        };

        mousePressedEventHandler = mouseEvent ->
        {
            startX = -1;
            startY = -1;

            if (mouseEvent.getButton() != MouseButton.PRIMARY || containerMouseX == -1 && containerMouseY == -1)
            {
                return;
            }

            if (draggable != null)
            {
                draggable.setLastPressedWasDrag(false);
            }

            if (resizable != null)
            {
                resizable.setLastPressedWasResize(false);
            }

            if (resizable != null && resizable.canResize() && mouseOnEdge)
            {
                //They will be set to themselves here, no biggies.
                this.saveStartResizingValues(this.resizeXMultiplier, this.resizeYMultiplier, true);
            } else if (draggable != null && draggable.canDrag())
            {
                //Here getX and getY return the mouse position based on the control itself not on the container.
                //Width = 300, click in the middle = 150, top = 0, bottom = 300
                saveStartValues(mouseEvent.getX(), mouseEvent.getY());
            }
        };

        dragEventHandler = mouseEvent ->
        {
            if (mouseEvent.getButton() != MouseButton.PRIMARY || (startX == -1 && startY == -1))
            {
                return;
            }

            if (resizable != null && resizable.canResize() && mouseOnEdge)
            {
                resizable.setLastPressedWasResize(true);
                resizable.setIsResizing(true);

                this.doResizeWithContainerMouse(true);
            } else if (draggable != null && draggable.canDrag())
            {
                draggable.setLastPressedWasDrag(true);
                draggable.setIsDragged(true);

                var xDiff = mouseEvent.getX() - startX;
                var yDiff = mouseEvent.getY() - startY;
                this.move(xDiff, yDiff, true);
            }
        };

        mouseReleaseEventHandler = mouseEvent ->
        {
            boolean hasChanged = false;
            if (draggable != null && draggable.isDragged())
            {
                draggable.setIsDragged(false);
                hasChanged = true;
            }

            if (resizable != null && resizable.isResizing())
            {
                resizable.setIsResizing(false);
                hasChanged = true;
            }

            if (hasChanged)
            {
                var savedStartLayoutX = this.startLayoutX;
                var savedStartLayoutY = this.startLayoutY;
                var savedStartWidth = this.startWidth;
                var savedStartHeight = this.startHeight;
                UndoRedoManager.getInstance().addUndoAction(returnPaneOnly ->
                {
                    if (!returnPaneOnly)
                    {
                        this.targetRegion.relocate(savedStartLayoutX, savedStartLayoutY);
                        this.targetRegion.setPrefSize(savedStartWidth, savedStartHeight);
                    }
                    return undoRedoPane;
                });
            }
        };

        mouseMovedEventHandler = mouseEvent ->
        {
            mouseOnEdge = false;
            if (resizable == null || !resizable.canResize())
            {
                return;
            }

            resizeXMultiplier = resizeYMultiplier = 0;

            var x = mouseEvent.getX();
            var y = mouseEvent.getY();

            mouseOnEdge = true;
            if (x <= 5 && y <= 5)
            {
                resizeXMultiplier = -1;
                resizeYMultiplier = -1;
                targetRegion.setCursor(Cursor.NW_RESIZE);
            } else if (x <= 5 && y >= targetRegion.getHeight() - 5)
            {
                resizeXMultiplier = -1;
                resizeYMultiplier = 1;
                targetRegion.setCursor(Cursor.NE_RESIZE);
            } else if (x >= targetRegion.getWidth() - 5 && y >= targetRegion.getHeight() - 5)
            {
                resizeXMultiplier = 1;
                resizeYMultiplier = 1;
                targetRegion.setCursor(Cursor.SE_RESIZE);
            } else if (x >= targetRegion.getWidth() - 5 && y <= 5)
            {
                resizeXMultiplier = 1;
                resizeYMultiplier = -1;
                targetRegion.setCursor(Cursor.SW_RESIZE);
            } else if (y <= 5)
            {
                resizeXMultiplier = 0;
                resizeYMultiplier = -1;
                targetRegion.setCursor(Cursor.N_RESIZE);
            } else if (y >= targetRegion.getHeight() - 5)
            {
                resizeXMultiplier = 0;
                resizeYMultiplier = 1;
                targetRegion.setCursor(Cursor.S_RESIZE);
            } else if (x <= 5)
            {
                resizeXMultiplier = -1;
                resizeYMultiplier = 0;
                targetRegion.setCursor(Cursor.E_RESIZE);
            } else if (x >= targetRegion.getWidth() - 5)
            {
                resizeXMultiplier = 1;
                resizeYMultiplier = 0;
                targetRegion.setCursor(Cursor.H_RESIZE);
            } else
            {
                mouseOnEdge = false;
                targetRegion.setCursor(null);
                resizable.setIsResizing(false); //I will allow resize only on the contour of the target region
            }
        };

        mouseExitEventHandler = event ->
        {
            if (resizable != null && !resizable.isResizing())
            {
                mouseOnEdge = false;
                targetRegion.setCursor(null);
            }
        };
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        this.registerEvents();
    }

    public void setGroupedObjectIterable(@Nullable Iterable<? extends DragAndResizeObject> groupedObjectIterable)
    {
        this.groupedObjectIterable = groupedObjectIterable;
    }

    public void move(double xDiff, double yDiff, boolean alsoMoveGrouped)
    {
        //This whole below is to move multiple object without going out of the edge.
        //It iterates through all the selected
        //and keep the lowest difference of position (If one cannot move, the whole group won't move)
        var diffXY = new MutableXY(0, 0);
        this.calculateMoveLayout(this.targetRegion, xDiff, yDiff, diffXY);

        var minXDiff = diffXY.getX();
        var minYDiff = diffXY.getY();

        if (groupedObjectIterable == null || !alsoMoveGrouped)
        {
            this.targetRegion.setLayoutX(this.targetRegion.getLayoutX() + minXDiff);
            this.targetRegion.setLayoutY(this.targetRegion.getLayoutY() + minYDiff);
        } else
        {
            for (var groupedObject : groupedObjectIterable)
            {
                if (groupedObject != this) //Ignore the one already done (This one) that has priority.
                {
                    this.calculateMoveLayout(groupedObject.targetRegion, xDiff, yDiff, diffXY);
                    minXDiff = MathUtil.findNearestToZero(diffXY.getX(), minXDiff);
                    minYDiff = MathUtil.findNearestToZero(diffXY.getY(), minYDiff);
                }
            }

            if (minXDiff != 0 || minYDiff != 0)
            {
                //I don't care about check if is instance of this object, because it applies to every object
                //same instance included (Or if the collection is null, just apply to this one!)
                for (var groupedObject : groupedObjectIterable)
                {
                    var groupedTargetRegion = groupedObject.targetRegion;
                    groupedTargetRegion.setLayoutX(groupedTargetRegion.getLayoutX() + minXDiff);
                    groupedTargetRegion.setLayoutY(groupedTargetRegion.getLayoutY() + minYDiff);
                }
            }
        }

    }

    private void calculateMoveLayout(Region draggable, double xDiff, double yDiff, MutableXY diff)
    {
        var stroke = draggable.getBorder().getStrokes().get(0);

        var outerEdge = ReflectionUtil.getFieldValue(BORDER_STROKE_OUTER_EDGE, stroke, Insets.class);
        var borderEdge = outerEdge == null ? Insets.EMPTY : outerEdge;

        diff.setXY(0, 0);
        if (xDiff != 0)
        {
            var newLayoutX = draggable.getLayoutX() + xDiff;
            newLayoutX = Math.max(borderEdge.getLeft(), newLayoutX);
            newLayoutX = Math.min(containerRegion.getWidth() - draggable.getWidth() - borderEdge.getRight(), newLayoutX);
            diff.setX(newLayoutX - draggable.getLayoutX());
        }

        if (yDiff != 0)
        {
            var newLayoutY = draggable.getLayoutY() + yDiff;
            newLayoutY = Math.max(borderEdge.getTop(), newLayoutY);
            newLayoutY = Math.min(containerRegion.getHeight() - draggable.getHeight() - borderEdge.getBottom(), newLayoutY);
            diff.setY(newLayoutY - draggable.getLayoutY());
        }
    }

    /**
     * Resize all the selected objects based on the difference of this object from current to new size
     *
     * @param newWidth  The new width for this object or 0 to ignore
     * @param newHeight The new height for this object or 0 to ignore
     */
    public void resize(double newWidth, double newHeight, boolean alsoResizeGrouped)
    {
        //Cannot give a resize command from outside if RESIZING!
        if (resizable == null || resizable.isResizing() || mouseOnEdge)
        {
            return;
        }

        //Set this to 1 because i want a regular resize (Just change Width not Layout)
        this.saveStartResizingValues(1d, 1d, alsoResizeGrouped);
        this.doResize(
                newWidth == 0 ? 0 : (newWidth - startWidth),
                newHeight == 0 ? 0 : (newHeight - startHeight),
                alsoResizeGrouped
        );
    }

    void saveStartResizingValues(double resizeXMultiplier, double resizeYMultiplier, boolean alsoResizeGrouped)
    {
        this.resizeXMultiplier = resizeXMultiplier;
        this.resizeYMultiplier = resizeYMultiplier;

        saveStartValues(containerMouseX, containerMouseY);

        if (groupedObjectIterable != null && alsoResizeGrouped)
        {
            for (var groupedObject : groupedObjectIterable)
            {
                if (groupedObject != this)
                {
                    groupedObject.saveStartResizingValues(
                            resizeXMultiplier, resizeYMultiplier, false
                    );
                }
            }
        }
    }

    void doResizeWithContainerMouse(boolean alsoResizeGrouped)
    {
        //ContainerMouseX / MouseY are required here. While resizing you change the size and,
        //using coordinates based on the object itself, causes twitchness
        var mouseX = MathUtil.between(0, containerMouseX, containerRegion.getWidth());
        var mouseY = MathUtil.between(0, containerMouseY, containerRegion.getHeight());
        this.doResize(
                (mouseX - startX) * resizeXMultiplier,
                (mouseY - startY) * resizeYMultiplier,
                alsoResizeGrouped);
    }


    private void doResize(double xDiffFromStart, double yDiffFromStart, boolean alsoResizeGrouped)
    {
        boolean xValid = false, yValid = false;

        var containerWidth = containerRegion.getWidth();
        if (resizeXMultiplier != 0 && xDiffFromStart != 0)
        {
            var newWidth = startWidth + xDiffFromStart;
            newWidth = Math.max(newWidth, 10);
            newWidth = Math.min(newWidth, containerWidth); //Set the new width to be at max as big as the container
            if (resizeXMultiplier < 0)
            {
                //Managing it by size diff allow the size limits to take place even here
                var newLayoutX = startLayoutX + (startWidth - newWidth);
                newLayoutX = Math.min(containerWidth - newWidth, newLayoutX);
                newLayoutX = Math.max(0, newLayoutX);
                targetRegion.setLayoutX(newLayoutX);
            }

            newWidth = Math.min(newWidth, containerWidth - targetRegion.getLayoutX());
            targetRegion.setPrefWidth(newWidth);

            xValid = true;
        }

        var containerHeight = containerRegion.getHeight();
        if (resizeYMultiplier != 0 && yDiffFromStart != 0)
        {
            var newHeight = startHeight + yDiffFromStart;
            newHeight = Math.max(newHeight, 10);
            newHeight = Math.min(newHeight, containerHeight);
            if (resizeYMultiplier < 0)
            {
                //Managing it by size diff allow the size limits to take place even here
                var newLayoutY = startLayoutY + (startHeight - newHeight);
                //The min is first in case that value goes lower than zero is then fixed after
                newLayoutY = Math.min(containerHeight - newHeight, newLayoutY);
                newLayoutY = Math.max(0, newLayoutY);
                targetRegion.setLayoutY(newLayoutY);
            }

            newHeight = Math.min(newHeight, containerHeight - targetRegion.getLayoutY());
            targetRegion.setPrefHeight(newHeight);

            yValid = true;
        }

        if (groupedObjectIterable != null && alsoResizeGrouped && (xValid || yValid))
        {
            for (var groupedObject : groupedObjectIterable)
            {
                if (groupedObject != this)
                {
                    groupedObject.doResizeWithContainerMouse(false);
                }
            }
        }
    }

    private void saveStartValues(double startX, double startY)
    {
        this.startX = startX;
        this.startY = startY;

        startLayoutX = targetRegion.getLayoutX();
        startLayoutY = targetRegion.getLayoutY();

        startWidth = targetRegion.getWidth();
        startHeight = targetRegion.getHeight();
    }

    public void registerEvents()
    {
        containerRegion.addEventFilter(MouseEvent.ANY, containerMouseAnyEvent);

        targetRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExitEventHandler);
    }

    private void unregisterEvents()
    {
        containerRegion.removeEventFilter(MouseEvent.ANY, containerMouseAnyEvent);

        targetRegion.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleaseEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_EXITED, mouseExitEventHandler);
    }
}

