package parozzz.github.com.hmi.page;

import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

import java.io.IOException;

public abstract class PaneHMIStage<P extends Pane> extends HMIStage<P>
{
    public PaneHMIStage(String name, String resource, Class<P> paneClass) throws IOException
    {
        super(name, resource, paneClass);
    }

    public PaneHMIStage(String name, P pane)
    {
        super(name, pane);
    }

    @Override
    public void setup()
    {
        super.setup();
    }

    public void setBackground(Background background)
    {
        parent.setBackground(background);
    }

    public void addChildren(Node... nodes)
    {
        parent.getChildren().addAll(nodes);
    }

    public void removeChildren(Node... node)
    {
        parent.getChildren().removeAll(node);
    }

    public void addToPane(Pane pane)
    {
        pane.getChildren().add(this.parent);
    }

    public void removeFromPane(Pane pane)
    {
        pane.getChildren().remove(this.parent);
    }

}
