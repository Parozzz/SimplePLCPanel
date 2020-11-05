package parozzz.github.com.hmi.main.dragdrop;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.main.MainEditStage;


public final class DraggableControlCreator<C extends Control> extends FXController
{
    private boolean started = false;
    private Cursor oldCursor;

    private final MainEditStage mainMenuPage;
    private final Button creatorButton;
    private final ControlWrapperType<C, ?> wrapperType;

    public DraggableControlCreator(MainEditStage mainMenuPage, String name, Button creatorButton, ControlWrapperType<C, ?> wrapperType)
    {
        super(name);

        this.mainMenuPage = mainMenuPage;
        this.creatorButton = creatorButton;
        this.wrapperType = wrapperType;
    }

    @Override
    public void setup()
    {
        super.setup();

        creatorButton.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            if(mainMenuPage.getShownControlContainer() == null)
            {
                return;
            }

            started = true;

            var stageSetter = mainMenuPage.getStageSetter();
            oldCursor = stageSetter.getCursor();
            stageSetter.setCursor(Cursor.CLOSED_HAND);
        });

        creatorButton.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent ->
        {
            if (started)
            {
                started = false;

                var controlMainPage = mainMenuPage.getShownControlContainer();
                if(controlMainPage != null)
                {
                    var anchorPane = controlMainPage.getMainAnchorPane();
                    //Only do it if inside the right pane
                    var pickResult = mouseEvent.getPickResult();
                    if (pickResult.getIntersectedNode() == anchorPane)
                    {
                        var point = pickResult.getIntersectedPoint();
                        var x = point.getX();
                        var y = point.getY();
                        if (x >= 0 && x <= anchorPane.getWidth() && y >= 0 && y <= anchorPane.getHeight())
                        {
                            var controlWrapper = controlMainPage.createControlWrapper(wrapperType);
                            controlWrapper.getContainerPane().relocate(x, y);
                        }
                    }
                }

                mainMenuPage.getStageSetter().setCursor(oldCursor);
                oldCursor = null;
            }
        });
    }
}
