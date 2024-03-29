package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.scene.Node;
import javafx.scene.control.*;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.function.Consumer;

public final class ContextMenuBuilder
{
    public static ContextMenuBuilder builder(ContextMenu contextMenu)
    {
        return new ContextMenuBuilder(contextMenu);
    }

    public static ContextMenuBuilder builder()
    {
        return new ContextMenuBuilder();
    }

    private final ContextMenu contextMenu;

    private ContextMenuBuilder(ContextMenu contextMenu)
    {
        this.contextMenu = contextMenu;
    }

    private ContextMenuBuilder()
    {
        this(new ContextMenu());
    }

    public ContextMenuBuilder spacer()
    {
        return this.spacer(1);
    }

    public ContextMenuBuilder spacer(int amount)
    {
        for(var x = 0; x < amount; x++)
        {
            contextMenu.getItems().add(new SeparatorMenuItem());
        }
        return this;
    }

    public ContextMenuBuilder simple(String text, Runnable action)
    {
        var menuItem = new MenuItem(text);
        menuItem.setOnAction(actionEvent -> action.run());
        contextMenu.getItems().add(menuItem);

        return this;
    }

    public ContextMenuBuilder simpleWithConsumer(String text, Consumer<MenuItem> consumer)
    {
        var menuItem = new MenuItem(text);
        menuItem.setOnAction(actionEvent -> consumer.accept(menuItem));
        contextMenu.getItems().add(menuItem);
        return this;
    }


    public ContextMenuBuilder custom(Node node, boolean hideOnClick)
    {
        return this.custom(node, hideOnClick, t -> {});
    }

    public ContextMenuBuilder button(String text, boolean hideOnClick, Runnable buttonPressAction)
    {
        return this.custom(new Button(), hideOnClick, button ->
        {
            button.setText(text);
            button.setOnAction(event -> buttonPressAction.run());
        });
    }

    public ContextMenuBuilder textField(boolean hideOnClick, Consumer<TextField> nodeConsumer)
    {
        return this.custom(new TextField(), hideOnClick, nodeConsumer);
    }

    public ContextMenuBuilder colorPicker(boolean hideOnClick, Consumer<ColorPicker> nodeConsumer)
    {
        return this.custom(new ColorPicker(), hideOnClick, nodeConsumer);
    }

    public <T extends Node> ContextMenuBuilder custom(T node, boolean hideOnClick, Consumer<T> nodeConsumer)
    {
        var customMenuItem = new CustomMenuItem(node);
        customMenuItem.setHideOnClick(hideOnClick);
        customMenuItem.setOnAction(event -> nodeConsumer.accept(node));
        customMenuItem.getStyleClass().add(Util.getResource("stylesheets/ContextMenu/focusless.css").toExternalForm());
        contextMenu.getItems().add(customMenuItem);

        return this;
    }

    public void setTo(Control control)
    {
        control.setContextMenu(contextMenu);
    }

    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }
}
