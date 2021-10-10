package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.ReflectionUtil;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public static boolean validateAllFXML(Object obj)
    {
        var clazz = obj.getClass();
        for(var field : clazz.getDeclaredFields())
        {
            field.trySetAccessible();

            var isFXML = field.isAnnotationPresent(FXML.class);
            if(isFXML && ReflectionUtil.getFieldValue(field, obj) == null)
            {
                MainLogger.getInstance().error("An object has failed FXML validation. One field value is null. " +
                        "Class: " + clazz.getSimpleName() + ". FieldName: " + field.getName(), null);
                return false;
            }
        }

        return true;
    }

    private static Map<Class<?>, Set<Field>> CASHED_FXML_VALIDATION_MAP;
    public static boolean validateAllFXMLWithCashing(Object obj)
    {
        if(CASHED_FXML_VALIDATION_MAP == null)
        {
            CASHED_FXML_VALIDATION_MAP = new IdentityHashMap<>();
        }

        var clazz = obj.getClass();

        var fieldSet = CASHED_FXML_VALIDATION_MAP.get(clazz);
        if(fieldSet == null)
        {
            CASHED_FXML_VALIDATION_MAP.put(clazz, fieldSet = new HashSet<>());
            Stream.of(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(FXML.class))
                    .peek(AccessibleObject::trySetAccessible)
                    .forEach(fieldSet::add);
        }

        for(var field : fieldSet)
        {
            if(ReflectionUtil.getFieldValue(field, obj) == null)
            {
                return false;
            }
        }

        return true;
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

    public static class BorderBuilder
    {
        private final Color[] colorArray;
        private final int[] widthArray;

        public BorderBuilder()
        {
            colorArray = new Color[4];
            widthArray = new int[4];
        }

        public BorderBuilder top(Color color, int width)
        {
            return set(0, color, width);
        }

        public BorderBuilder right(Color color, int width)
        {
            return set(1, color, width);
        }

        public BorderBuilder bottom(Color color, int width)
        {
            return set(2, color, width);
        }

        public BorderBuilder left(Color color, int width)
        {
            return set(3, color, width);
        }

        private BorderBuilder set(int index, Color color, int width)
        {
            colorArray[index] = color;
            widthArray[index] = width;
            return this;
        }

        public Border createBorder()
        {
            var borderWidths = new BorderWidths(widthArray[0], widthArray[1], widthArray[2], widthArray[3]);

            var stroke = new BorderStroke(
                    colorArray[0], colorArray[1], colorArray[2], colorArray[3],
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, borderWidths, Insets.EMPTY
            );
            return new Border(stroke);
        }
    }
}
