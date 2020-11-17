package parozzz.github.com.hmi.util.specialfunction;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import parozzz.github.com.hmi.util.DoubleClickable;
import parozzz.github.com.hmi.util.Resizable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;

public final class FXSpecialFunctionManager
{
    public static Builder builder(Region targetRegion)
    {
        return new Builder(targetRegion, null);
    }

    public static Builder builder(Region targetRegion, Pane containerPane)
    {
        return new Builder(targetRegion, containerPane);
    }

    private final Region targetRegion;
    private final Pane containerPane;

    private ResizeFunction resizeFunction;
    private DragFunction dragFunction;
    private DoubleClickFunction doubleClickFunction;

    private double containerMouseX;
    private double containerMouseY;

    private final EventHandler<MouseEvent> containerAnyMouseEventHandler;
    private final EventHandler<MouseEvent> mousePressedEventHandler;
    private final EventHandler<MouseEvent> mouseReleasedEventHandler;
    private final EventHandler<MouseEvent> mouseMovedEventHandler;
    private final EventHandler<MouseEvent> mouseDraggedEventHandler;
    private final EventHandler<MouseEvent> mouseExitEventHandler;

    public FXSpecialFunctionManager(Region targetRegion, Pane containerPane)
    {
        this.targetRegion = targetRegion;
        this.containerPane = containerPane;

        containerAnyMouseEventHandler = mouseEvent ->
        {
            containerMouseX = mouseEvent.getX();
            containerMouseY = mouseEvent.getY();
        };

        mousePressedEventHandler = mouseEvent ->
        {
            if (resizeFunction != null && resizeFunction.isOnEdge())
            {
                resizeFunction.onMousePressed(mouseEvent);
                //In case i am resizing, i ignore everything else
                if (resizeFunction.isOnEdge())
                {
                    return;
                }
            }

            if (dragFunction != null)
            {
                dragFunction.onMousePressed(mouseEvent);
            }
        };

        mouseReleasedEventHandler = mouseEvent ->
        {
            if (doubleClickFunction != null)
            {
                doubleClickFunction.onMouseReleased(mouseEvent);
            }

            if(resizeFunction != null)
            {
                resizeFunction.onMouseReleased(mouseEvent);
            }
        };

        mouseMovedEventHandler = mouseEvent ->
        {
            if (resizeFunction != null)
            {
                resizeFunction.onMouseMoved(mouseEvent);
            }
        };

        mouseDraggedEventHandler = mouseEvent ->
        {
            if (resizeFunction != null)
            {
                resizeFunction.onMouseDragged(mouseEvent);
                if (resizeFunction.isOnEdge())
                {
                    return;
                }
            }

            if (dragFunction != null)
            {
                dragFunction.onMouseDragged(mouseEvent);
            }
        };

        mouseExitEventHandler = mouseEvent ->
        {
            if(resizeFunction != null)
            {
                resizeFunction.onMouseExit(mouseEvent);
            }
        };
    }

    public void bind()
    {
        if (containerPane != null)
        {
            containerPane.addEventFilter(MouseEvent.ANY, containerAnyMouseEventHandler);
        }

        targetRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
        targetRegion.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExitEventHandler);
    }

    public void unbind()
    {
        if (containerPane != null)
        {
            containerPane.removeEventFilter(MouseEvent.ANY, containerAnyMouseEventHandler);
        }

        targetRegion.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
        targetRegion.removeEventFilter(MouseEvent.MOUSE_EXITED, mouseExitEventHandler);
    }

    double getContainerMouseX()
    {
        return containerMouseX;
    }

    double getContainerMouseY()
    {
        return containerMouseY;
    }

    Region getTargetRegion()
    {
        return targetRegion;
    }

    Pane getContainerPane()
    {
        return containerPane;
    }

    public static class Builder
    {
        private final FXSpecialFunctionManager specialFunctionManager;

        private Builder(Region targetRegion, Pane containerPane)
        {
            specialFunctionManager = new FXSpecialFunctionManager(targetRegion, containerPane);
        }

        public Builder enableResizing(Resizable resizable,
                DoubleConsumer newWidthConsumer, DoubleConsumer newHeightConsumer)
        {
            Objects.requireNonNull(specialFunctionManager.containerPane, "ContainerPane is required for Resizing");

            specialFunctionManager.resizeFunction = new ResizeFunction(specialFunctionManager, resizable);

            var resizeFunction = specialFunctionManager.resizeFunction;
            resizeFunction.newWidthConsumer = newWidthConsumer;
            resizeFunction.newHeightConsumer = newHeightConsumer;
            return this;
        }

        public Builder enableDrag(DoubleConsumer newXConsumer, DoubleConsumer newYConsumer)
        {
            Objects.requireNonNull(specialFunctionManager.containerPane, "ContainerPane is required for Dragging");

            specialFunctionManager.dragFunction = new DragFunction(specialFunctionManager, newXConsumer, newYConsumer);
            return this;
        }

        public Builder enableDoubleClick(MouseButton mouseButton, DoubleClickable doubleClickable, Runnable runnable)
        {
            specialFunctionManager.doubleClickFunction = new DoubleClickFunction(mouseButton, doubleClickable, runnable);
            return this;
        }

        public FXSpecialFunctionManager get()
        {
            return specialFunctionManager;
        }

        public FXSpecialFunctionManager bind()
        {
            specialFunctionManager.bind();
            return specialFunctionManager;
        }
    }
}
