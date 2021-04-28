package parozzz.github.com.simpleplcpanel.hmi.util.specialfunction;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.DoubleConsumer;

public final class DragFunction
{
    private final FXSpecialFunctionManager specialFunctionManager;

    private double startX;
    private double startY;

    private final DoubleConsumer newXConsumer;
    private final DoubleConsumer newYConsumer;

    DragFunction(FXSpecialFunctionManager specialFunctionManager, DoubleConsumer newXConsumer, DoubleConsumer newYConsumer)
    {
        this.specialFunctionManager = specialFunctionManager;

        this.newXConsumer = newXConsumer;
        this.newYConsumer = newYConsumer;
    }

    public void onMousePressed(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton() != MouseButton.PRIMARY)
        {
            startX = -1;
            startY = -1;
            return;
        }

        startX = mouseEvent.getX();
        startY = mouseEvent.getY();
    }

    void onMouseDragged(MouseEvent mouseEvent)
    {
        if(startX == -1 || startY == -1 || mouseEvent.getButton() != MouseButton.PRIMARY)
        {
            return;
        }

        var draggable = specialFunctionManager.getTargetRegion();
        var container = specialFunctionManager.getContainerPane();

        var newLayoutX = draggable.getLayoutX() + (mouseEvent.getX() - startX);
        var newLayoutY = draggable.getLayoutY() + (mouseEvent.getY() - startY);

        newLayoutX = Math.max(0, newLayoutX);
        newLayoutX = Math.min(container.getWidth() - draggable.getWidth(), newLayoutX);

        newLayoutY = Math.max(0, newLayoutY);
        newLayoutY = Math.min(container.getHeight() - draggable.getHeight(), newLayoutY);

        newXConsumer.accept(newLayoutX);
        newYConsumer.accept(newLayoutY);
        //draggable.relocate(newLayoutX, newLayoutY);

        //I don't need to update the startX/startY values here otherwise the drag is glitchy and laggy
    }
}
