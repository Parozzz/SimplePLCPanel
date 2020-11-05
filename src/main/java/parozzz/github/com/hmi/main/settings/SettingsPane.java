package parozzz.github.com.hmi.main.settings;

import javafx.scene.Parent;
import parozzz.github.com.hmi.FXController;

public abstract class SettingsPane extends FXController
{
    public SettingsPane(String name)
    {
        super(name);
    }

    public abstract Parent getMainParent();
}
