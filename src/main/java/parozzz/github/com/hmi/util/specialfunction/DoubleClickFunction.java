package parozzz.github.com.hmi.util.specialfunction;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.hmi.util.DoubleClickable;
import parozzz.github.com.hmi.util.FXUtil;

final class DoubleClickFunction
{
    private final MouseButton mouseButton;
    private final DoubleClickable doubleClickable;
    private final Runnable runnable;

    private long lastClickTimestamp;

    public DoubleClickFunction(MouseButton mouseButton, DoubleClickable doubleClickable, Runnable runnable)
    {
        this.mouseButton = mouseButton;
        this.doubleClickable = doubleClickable;
        this.runnable = runnable;
    }

    void onMouseReleased(MouseEvent mouseEvent)
    {
        //If a click is done in a special way, do not count it as click and reset the timestamp
        if (mouseEvent.getButton() != mouseButton
                || mouseEvent.isControlDown() || mouseEvent.isAltDown()
                || mouseEvent.isShiftDown() || !doubleClickable.canDoubleClick())
        {
            lastClickTimestamp = 0;
            return;
        }

        //If first time or the time has passed, refresh the timestamp
        if (System.currentTimeMillis() - lastClickTimestamp > 350)
        {
            lastClickTimestamp = System.currentTimeMillis();
            return;
        }

        //If two clicks inside the 500ms of time, run thr runnable and reset the timestamp
        runnable.run();
        lastClickTimestamp = System.currentTimeMillis();
    }
}
