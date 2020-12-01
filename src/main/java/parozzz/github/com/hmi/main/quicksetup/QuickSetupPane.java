package parozzz.github.com.hmi.main.quicksetup;

import javafx.scene.Parent;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

public interface QuickSetupPane
{
    Parent getParent();

    void onNewControlWrapper(ControlWrapper<?> controlWrapper);

    void addBinders(QuickSetupStateBinder stateBinder);

    default void clear()
    {
        this.getParent().setVisible(false);
    }
}
