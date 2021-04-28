package parozzz.github.com.simpleplcpanel.hmi.main.settings;

import javafx.scene.Parent;
import parozzz.github.com.simpleplcpanel.hmi.FXController;

public abstract class SettingsPane extends FXController
{
    public SettingsPane(String name)
    {
        super(name);
    }

    public abstract Parent getMainParent();
}
