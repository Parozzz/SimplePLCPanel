package parozzz.github.com.hmi.main.quicksetup;

import javafx.scene.Parent;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

public interface QuickSetupPane
{
    Parent getMainParent();

    void onNewControlWrapper(ControlWrapper<?> controlWrapper);

    void onNewWrapperState(WrapperState wrapperState);

    void addBinders(QuickSetupStateBinder stateBinder);

    void clear();
}
