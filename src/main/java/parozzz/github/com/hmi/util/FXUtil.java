package parozzz.github.com.hmi.util;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import parozzz.github.com.util.Util;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;

public class FXUtil
{

    public static final KeyCombination CONTROL_COPY = KeyCombination.keyCombination("CTRL+C");
    public static final KeyCombination CONTROL_PASTE = KeyCombination.keyCombination("CTRL+V");
    public static final KeyCombination CONTROL_CUT = KeyCombination.keyCombination("CTRL+X");

    private final static NumberStringConverter numberStringConverter = new NumberStringConverter(Locale.ITALY);
    public static final FileChooser.ExtensionFilter IMAGE_EXTENSION_FILTER = new FileChooser.ExtensionFilter("Images (*.jpeg, *.jpg, *.png)", "*.jpeg", "*.jpg", "*.png");

    private FXUtil()
    {
    }

    public static Background createBackground(Color color)
    {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    public static Border createBorder(Color color, int width)
    {
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width)));
    }

    public static <T> StringConverter<T> toStringOnlyConverter(Function<T, String> function)
    {
        return new StringConverter<>() {
            @Override
            public String toString(T t)
            {
                return function.apply(t);
            }

            @Override
            public T fromString(String s)
            {
                return null;
            }
        };
    }

    private static void runEvery(Duration duration, Runnable runnable)
    {
        var timeline = new Timeline(new KeyFrame(duration, e -> runnable.run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    public static void runEveryMillis(int millis, Runnable runnable)
    {
        runEvery(Duration.millis(millis), runnable);
    }

    public static void runEverySecond(int second, Runnable runnable)
    {
        runEvery(Duration.seconds(second), runnable);
    }

    public static void runLater(int millis, Runnable runnable)
    {
        var timeline = new Timeline(new KeyFrame(Duration.millis(millis), e -> runnable.run()));
        timeline.setCycleCount(1);
        timeline.playFromStart();
    }

    public static Node hasId(Node node, String id)
    {
        return id.equals(node.getId()) ? node : null;
    }

    public static MenuItem hasId(MenuItem menuItem, String id)
    {
        return id.equals(menuItem.getId()) ? menuItem : null;
    }

    public static Parent loadFXML(String resource) throws IOException
    {
        return loadFXML(resource, null);
    }

    public static Parent loadFXML(String resource, Object controller) throws IOException
    {
        var fxmlLoader = new FXMLLoader(Util.getResource(resource));
        if (controller != null)
        {
            fxmlLoader.setController(controller);
        }

        return fxmlLoader.load();
    }

    /*
    public static void showExceptionAlert(String message, Exception exception)
    {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
        alert.getDialogPane().getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .forEach(label -> label.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE));
        alert.setHeaderText("An exception has occurred");

        var exceptionStringBuilder = new StringBuilder();
        exceptionStringBuilder.append(message).append('\n');

        var exceptionMessage = exception.getMessage();
        if(exceptionMessage != null)
        {
            exceptionStringBuilder.append(exceptionMessage).append('\n');
        }

        for(var trace : exception.getStackTrace())
        {
            exceptionStringBuilder.append(trace.toString()).append('\n');
        }

        alert.setContentText(exceptionStringBuilder.toString());
        alert.show();
    }*/

    public static <T extends Event> EventHandler<T> appendEventHandler(Class<T> eventClass, EventHandler<T> oldEventHandler,
                                                                       EventHandler<T> newEventHandler)
    {
        if (oldEventHandler != null)
        {
            return event ->
            {
                newEventHandler.handle(event);
                oldEventHandler.handle(event);
            };
        }

        return newEventHandler;
    }
}
