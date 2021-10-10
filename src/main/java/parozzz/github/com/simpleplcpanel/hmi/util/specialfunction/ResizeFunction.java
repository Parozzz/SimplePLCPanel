package parozzz.github.com.simpleplcpanel.hmi.util.specialfunction;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.simpleplcpanel.hmi.util.Resizable;

final class ResizeFunction
{
    private final FXSpecialFunctionManager specialFunctionManager;
    private final Resizable resizable;

    private double startX;
    private double startY;

    private double startWidth;
    private double startHeight;

    private double startLayoutX;
    private double startLayoutY;

    private double xMultiplier;
    private double yMultiplier;

    private boolean onEdge;

    ResizeFunction(FXSpecialFunctionManager specialFunctionManager, Resizable resizable)
    {
        this.specialFunctionManager = specialFunctionManager;
        this.resizable = resizable;
    }

    public boolean isOnEdge()
    {
        return onEdge;
    }

    void onMousePressed(MouseEvent mouseEvent)
    {
        if(!onEdge || mouseEvent.getButton() != MouseButton.PRIMARY)
        {
            startX = -1;
            startY = -1;
            return;
        }

        resizable.setLastPressedWasResize(true);
        resizable.setIsResizing(true);
        //resizing = true;

        var resizable = specialFunctionManager.getTargetRegion();
        startWidth = resizable.getWidth();
        startHeight = resizable.getHeight();

        startLayoutX = resizable.getLayoutX();
        startLayoutY = resizable.getLayoutY();

        startX = specialFunctionManager.getContainerMouseX();
        startY = specialFunctionManager.getContainerMouseY();
    }

    void onMouseReleased(MouseEvent mouseEvent)
    {
        if(!onEdge || mouseEvent.getButton() != MouseButton.PRIMARY)
        {
            return;
        }

        resizable.setIsResizing(false);
        //resizing = false;
    }

    void onMouseDragged(MouseEvent mouseEvent)
    {
        if(!onEdge || startX == -1 || startY == 1)
        {
            return;
        }

        this.parseResizeX();
        this.parseResizeY();

        //JOKE ON YOU, SAME AS DRAG!
    }

    private void parseResizeX()
    {
        var resizable = specialFunctionManager.getTargetRegion();
        var container = specialFunctionManager.getContainerPane();

        var containerWidth = container.getWidth();
        if(xMultiplier != 0)
        {
            var mouseX = specialFunctionManager.getContainerMouseX();
            mouseX = Math.min(mouseX, containerWidth);
            mouseX = Math.max(mouseX, 0);

            var xDiff = (mouseX - startX) * xMultiplier;

            var newWidth = startWidth + xDiff;
            newWidth = Math.max(newWidth, 10);
            newWidth = Math.min(newWidth, containerWidth); //Set the new width to be at max as big as the container
            if(xMultiplier < 0)
            {
                //Managing it by size diff allow the size limits to take place even here
                var newLayoutX = startLayoutX + (startWidth - newWidth);
                newLayoutX = Math.min(containerWidth - newWidth, newLayoutX);
                newLayoutX = Math.max(0, newLayoutX);
                resizable.setLayoutX(newLayoutX);
            }

            newWidth = Math.min(newWidth, containerWidth - resizable.getLayoutX());
            resizable.setPrefWidth(newWidth);
        }
    }

    private void parseResizeY()
    {
        var resizable = specialFunctionManager.getTargetRegion();
        var container = specialFunctionManager.getContainerPane();

        var containerHeight = container.getHeight();
        if(yMultiplier != 0)
        {
            var mouseY = specialFunctionManager.getContainerMouseY();
            mouseY = Math.min(mouseY, containerHeight);
            mouseY = Math.max(mouseY, 0);

            var yDiff = (mouseY - startY) * yMultiplier;

            var newHeight = startHeight + yDiff;
            newHeight = Math.max(newHeight, 10);
            newHeight = Math.min(newHeight, containerHeight);
            if(yMultiplier < 0)
            {
                //Managing it by size diff allow the size limits to take place even here
                var newLayoutY = startLayoutY + (startHeight - newHeight);
                //The min is first in case that value goes lower than zero is then fixed after
                newLayoutY = Math.min(containerHeight - newHeight, newLayoutY);
                newLayoutY = Math.max(0, newLayoutY);

                resizable.setLayoutY(newLayoutY);
            }

            newHeight = Math.min(newHeight, containerHeight - resizable.getLayoutY());
            resizable.setPrefHeight(newHeight);
        }
    }

    void onMouseMoved(MouseEvent mouseEvent)
    {
        var targetRegion = specialFunctionManager.getTargetRegion();
        if(!resizable.canResize())
        {
            return;
        }

        onEdge = true;
        xMultiplier = yMultiplier = 0;

        var x = mouseEvent.getX();
        var y = mouseEvent.getY();
        if(y <= 5)
        {
            yMultiplier = -1;
            targetRegion.setCursor(Cursor.N_RESIZE);
        }else if(y >= targetRegion.getHeight() - 5)
        {
            yMultiplier = 1;
            targetRegion.setCursor(Cursor.S_RESIZE);
        }else if(x <= 5)
        {
            xMultiplier = -1;
            targetRegion.setCursor(Cursor.E_RESIZE);
        }else if(x >= targetRegion.getWidth() - 5)
        {
            xMultiplier = 1;
            targetRegion.setCursor(Cursor.H_RESIZE);
        }else
        {
            targetRegion.setCursor(null);
            onEdge = false;
        }
    }

    void onMouseExit(MouseEvent mouseEvent)
    {
        if(!resizable.isResizing())
        {
            specialFunctionManager.getTargetRegion().setCursor(null);
            onEdge = false;
        }
    }
}
