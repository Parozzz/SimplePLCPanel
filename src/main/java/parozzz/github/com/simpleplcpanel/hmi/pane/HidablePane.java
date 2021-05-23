package parozzz.github.com.simpleplcpanel.hmi.pane;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public interface HidablePane
{
    BooleanProperty visibleProperty();

    default void bindVisibilityToMenuItem(MenuItem menuItem,
            Runnable showRunnable,
            Runnable hideRunnable)
    {
        var visible = visibleProperty();
        menuItem.setOnAction(event -> visible.set(true));
        visible.addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
            {
                showRunnable.run();
            }
            else
            {
                hideRunnable.run();
            }
        });

        if(visible.get())
        {
            showRunnable.run();
        }
        else
        {
            hideRunnable.run();
        }
    }

    default void show()
    {
        visibleProperty().set(true);
    }

    default void hide()
    {
        visibleProperty().set(false);
    }

    default Parent createHideParent(Pos alignment)
    {
        var hideButton = new Button("X");
        hideButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        hideButton.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        hideButton.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 14));
        hideButton.setTextFill(Color.RED);
        hideButton.setPadding(new Insets(1));
        hideButton.setPrefSize(25, 25);
        hideButton.setOnAction(event -> this.hide());

        var hideButtonStackPane = new StackPane(hideButton);
        hideButtonStackPane.setAlignment(alignment);
        hideButtonStackPane.setMinSize(0, 0);
        hideButtonStackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return hideButtonStackPane;
    }
}
