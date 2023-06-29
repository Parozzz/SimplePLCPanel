package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.extra;

import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;


public class ChangePageExtraFeature implements ControlWrapperExtraFeature
{
    private final ControlWrapper<?> controlWrapper;
    private final Control control;

    private final EventHandler<MouseEvent> eventHandler;

    public ChangePageExtraFeature(ControlWrapper<?> controlWrapper, Control control, String pageName)
    {
        this.controlWrapper = controlWrapper;
        this.control = control;

        eventHandler = mouseEvent -> {
            var menuPage = controlWrapper.getControlMainPage().getMainEditStage();
            if(!menuPage.isRuntimeShowing())
            {
                return;
            }

            var controlWrapperMainPage = controlWrapper.getControlMainPage().getControlContainerDatabase().getByName(pageName);
            menuPage.changeRuntimePage(controlWrapperMainPage);
        };
    }

    @Override
    public void bind()
    {
        control.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
    }

    @Override
    public void unbind()
    {
        control.removeEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
    }
}
