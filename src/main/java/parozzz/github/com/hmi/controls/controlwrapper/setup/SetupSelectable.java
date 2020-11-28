package parozzz.github.com.hmi.controls.controlwrapper.setup;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.util.FXUtil;

public interface SetupSelectable
{
    Button getSelectButton();

    Parent getParent();
}
