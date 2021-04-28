package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.scene.control.SplitPane;
import parozzz.github.com.simpleplcpanel.util.primitiveObject.BooleanObject;

public final class FXPaneUtil
{
    private FXPaneUtil() {}


    public static void setSplitPaneImmutableDivider(SplitPane splitPane)
    {
        for (var divider : splitPane.getDividers())
        {
            var avoidInfiniteLoop = new BooleanObject();
            //This will stop resizable divider between panes
            divider.positionProperty().addListener((observableValue, oldValue, newValue) ->
            {
                if(avoidInfiniteLoop.get())
                {
                    return;
                }

                avoidInfiniteLoop.set();
                divider.setPosition(oldValue.doubleValue());
                avoidInfiniteLoop.reset();
            });
        }
    }
}
