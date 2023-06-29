package parozzz.github.com.simpleplcpanel.hmi.controls.quicksetup;

import javafx.scene.Parent;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;

public interface QuickSetupPanePart
{
    Parent getParent();

    boolean isControlWrapperValid(ControlWrapper<?> controlWrapper);

    void clearControlWrapper();

    void addBinders(QuickSetupStateBinder stateBinder);
}
