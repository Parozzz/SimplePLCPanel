package parozzz.github.com.hmi.page;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public final class HMIStageSetter
{
    private final Scene scene;
    private final Stage stage;

    public HMIStageSetter(Parent parent)
    {
        this.scene = new Scene(parent);

        this.stage = new Stage();
        stage.setScene(scene);
    }

    public HMIStageSetter initModality(Modality modality)
    {
        stage.initModality(modality);
        return this;
    }

    public HMIStageSetter initStyle(StageStyle style)
    {
        stage.initStyle(style);
        return this;
    }

    public HMIStageSetter setAlwaysOnTop(boolean alwaysOnTop)
    {
        stage.setAlwaysOnTop(alwaysOnTop);
        return this;
    }

    public boolean isShowing()
    {
        return stage.isShowing();
    }

    public double getWidth()
    {
        return stage.getWidth();
    }

    public double getHeight()
    {
        return stage.getHeight();
    }

    public HMIStageSetter resize(double width, double height)
    {
        stage.setWidth(width);
        stage.setHeight(height);
        return this;
    }

    public HMIStageSetter setMinSize(double minWidth, double minHeight)
    {
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        return this;
    }

    public HMIStageSetter setMaxSize(double maxWidth, double maxHeight)
    {
        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);
        return this;
    }

    public boolean isResizable()
    {
        return stage.isResizable();
    }

    public HMIStageSetter setResizable(boolean resizable)
    {
        stage.setResizable(resizable);
        return this;
    }

    public HMIStageSetter setMaximized(boolean maximized)
    {
        stage.setMaximized(maximized);
        return this;
    }

    public String getTitle()
    {
        return stage.getTitle();
    }

    public HMIStageSetter setTitle(String title)
    {
        stage.setTitle(title);
        return this;
    }

    public HMIStageSetter setWidth(double width)
    {
        stage.setWidth(width);
        return this;
    }

    public HMIStageSetter setHeight(double height)
    {
        stage.setHeight(height);
        return this;
    }

    public HMIStageSetter setFullScreen(boolean fullScreen, String fullScreenExitHint, KeyCombination keyCombination)
    {
        stage.setFullScreen(fullScreen);
        stage.setFullScreenExitHint(fullScreenExitHint);
        stage.setFullScreenExitKeyCombination(keyCombination);
        return this;
    }

    public HMIStageSetter setCursor(Cursor cursor)
    {
        scene.setCursor(cursor);
        return this;
    }

    public Cursor getCursor()
    {
        return scene.getCursor();
    }


    public <T extends Event> HMIStageSetter addEventHandler(EventType<T> eventType, EventHandler<T> eventHandler)
    {
        stage.addEventHandler(eventType, eventHandler);
        return this;
    }

    public <T extends Event> HMIStageSetter addEventFilter(EventType<T> eventType, EventHandler<T> eventHandler)
    {
        stage.addEventFilter(eventType, eventHandler);
        return this;
    }

    public HMIStageSetter setOnWindowCloseRequest(EventHandler<WindowEvent> eventHandler)
    {
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, eventHandler);
        return this;
    }

    public void hide()
    {
        this.close();
    }

    public void close()
    {
        stage.close();
    }

    public void show()
    {
        if(stage.isShowing())
        {
            if(stage.isIconified())
            {
                stage.setIconified(false);
            }

            var alwaysOnTop = stage.isAlwaysOnTop();
            stage.setAlwaysOnTop(!alwaysOnTop);
            stage.setAlwaysOnTop(alwaysOnTop);
        }

        stage.show();
    }

    public Stage get()
    {
        return stage;
    }
}
