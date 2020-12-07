package parozzz.github.com.hmi.main.quicksetup;

import javafx.scene.Parent;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

public interface QuickSetupPane
{
    Parent getParent();

    boolean validateControlWrapper(ControlWrapper<?> controlWrapper);

    void clearControlWrapper();

    void addBinders(QuickSetupStateBinder stateBinder);
}
