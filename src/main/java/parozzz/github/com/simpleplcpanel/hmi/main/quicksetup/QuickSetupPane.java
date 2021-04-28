package parozzz.github.com.simpleplcpanel.hmi.main.quicksetup;

import javafx.scene.Parent;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;

public interface QuickSetupPane
{
    Parent getParent();

    boolean validateControlWrapper(ControlWrapper<?> controlWrapper);

    void clearControlWrapper();

    void addBinders(QuickSetupStateBinder stateBinder);
}
