package parozzz.github.com.simpleplcpanel.hmi.pane;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public abstract class BorderPaneHMIStage extends HMIStage<BorderPane>
{

    public BorderPaneHMIStage(String name, String resource) throws IOException
    {
        super(name, resource, BorderPane.class);
    }

    public BorderPaneHMIStage(String name, BorderPane parent)
    {
        super(name, parent);
    }

    public void setTop(Node node)
    {
        parent.setTop(node);
    }

    public void setLeft(Node node)
    {
        parent.setLeft(node);
    }

    public void setRight(Node node)
    {
        parent.setRight(node);
    }

    public void setCenter(Node node)
    {
        parent.setCenter(node);
    }

    public void setBottom(Node node)
    {
        parent.setBottom(node);
    }

}
