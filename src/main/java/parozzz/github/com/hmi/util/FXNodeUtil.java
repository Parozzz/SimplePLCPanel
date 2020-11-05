package parozzz.github.com.hmi.util;

import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import parozzz.github.com.logger.MainLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FXNodeUtil
{
    private static Field TEXT_NODE_FIELD;
    private static Method TEXT_FIELD_FILL_METHOD;
    private static Method TEXT_FIELD_HIDE_CARET;
    private static Field CARET_PATH_FIELD;

    public static Path getCaret(TextField textField)
    {
        if(CARET_PATH_FIELD == null)
        {
            try
            {
                CARET_PATH_FIELD = TextInputControlSkin.class.getDeclaredField("caretPath");
                CARET_PATH_FIELD.trySetAccessible();
            } catch (NoSuchFieldException exception)
            {
                MainLogger.getInstance().error("Error while getting Caret of TextField", exception, null);
                return null;
            }
        }

        var skin = textField.getSkin();
        if(skin instanceof TextInputControlSkin)
        {
            try
            {
                return (Path) CARET_PATH_FIELD.get(skin);
            } catch (IllegalAccessException exception)
            {
                MainLogger.getInstance().error("Error while getting Caret of TextField", exception, null);
            }
        }

        return null;
    }

    public static void setTextFieldFill(TextField textField, Paint paint)
    {
        if (TEXT_FIELD_FILL_METHOD == null)
        {
            try
            {
                TEXT_FIELD_FILL_METHOD = TextInputControlSkin.class.getDeclaredMethod("setTextFill", Paint.class);
                TEXT_FIELD_FILL_METHOD.trySetAccessible();
            } catch (NoSuchMethodException exception)
            {
                MainLogger.getInstance().error("Error while setting color of TextField", exception, null);
                return;
            }
        }

        var skin = textField.getSkin();
        if(skin instanceof TextInputControlSkin)
        {
            try
            {
                TEXT_FIELD_FILL_METHOD.invoke(skin, paint);
            } catch (IllegalAccessException | InvocationTargetException exception)
            {
                MainLogger.getInstance().error("Error while setting color of TextField", exception, null);
            }
        }
    }

    public static Text getTextFieldText(TextField textField)
    {
        if (TEXT_NODE_FIELD == null)
        {
            try
            {
                TEXT_NODE_FIELD = TextFieldSkin.class.getDeclaredField("textNode");
                TEXT_NODE_FIELD.trySetAccessible();
            } catch (NoSuchFieldException exception)
            {
                MainLogger.getInstance().error("Error while getting Text object from TextField", exception, null);
                return null;
            }
        }

        var skin = textField.getSkin();
        if (skin instanceof TextFieldSkin)
        {
            try
            {
                return (Text) TEXT_NODE_FIELD.get(skin);
            } catch (IllegalAccessException exception)
            {
                MainLogger.getInstance().error("Error while getting Text object from TextField", exception, null);
            }
        }

        return null;
    }

    private FXNodeUtil()
    {

    }

    /*

    public static void setResizable(Region resizable, Region container,
            BooleanSupplier canExecute,
            double minWidth, double minHeight,
            DoubleConsumer newWidthConsumer, DoubleConsumer newHeightConsumer)
    {
        var startResizeX = new NumberObject<>(0d);
        var startResizeY = new NumberObject<>(0d);

        var isResizing = new BooleanObject();
        container.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            var pickEvent = mouseEvent.getPickResult();
            if(pickEvent.getIntersectedNode() != resizable || mouseEvent.isConsumed() || !isResizing.get() || mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            mouseEvent.consume();

            var point = pickEvent.getIntersectedPoint();
            startResizeX.set(point.getX());
            startResizeY.set(point.getY());
        });

        resizable.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent ->
        {
            var pickEvent = mouseEvent.getPickResult();
            if(pickEvent.getIntersectedNode() != resizable || mouseEvent.isConsumed() || !isResizing.get() || mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            mouseEvent.consume();

            var point = pickEvent.getIntersectedPoint();

            var newWidth = resizable.getWidth() + (point.getX() - startResizeX.get());
            newWidth = Math.max(newWidth, minWidth);
            newWidthConsumer.accept(newWidth);

            var newHeight = resizable.getHeight() + (point.getY() - startResizeY.get());
            newHeight = Math.max(newHeight, minHeight);
            newHeightConsumer.accept(newHeight);

            resizable.setCursor(null);
            isResizing.reset();
        });

        container.addEventFilter(MouseEvent.MOUSE_MOVED, mouseEvent ->
        {
            if(mouseEvent.isConsumed())
            {
                return;
            }

            var pickResult = mouseEvent.getPickResult();
            if(pickResult.getIntersectedNode() == resizable)
            {
                if(!canExecute.getAsBoolean())
                {
                    return;
                }

                var point = pickResult.getIntersectedPoint();

                var x = point.getX();
                var y = point.getY();
                if(y <= 5)
                {
                    resizable.setCursor(Cursor.N_RESIZE);
                    isResizing.set();
                }
                else if(y >= resizable.getHeight() - 5)
                {
                    resizable.setCursor(Cursor.S_RESIZE);
                    isResizing.set();
                }
                else if(x <= 5)
                {
                    resizable.setCursor(Cursor.E_RESIZE);
                    isResizing.set();
                }
                else if(x >= resizable.getWidth() - 5)
                {
                    resizable.setCursor(Cursor.H_RESIZE);
                    isResizing.set();
                }
                else
                {
                    resizable.setCursor(null);
                    isResizing.reset();
                }
            }


        });
    }

    public static void setDrag(Region dragStartRegion, Region toDragRegion,  Region boundRegion)
    {
        setDrag(dragStartRegion, toDragRegion, boundRegion, () -> {});
    }

    public static void setDrag(Region dragStartRegion, Region toDragRegion, Region boundRegion,
            Runnable onDrag)
    {
        var oldDragX = new PrimitiveObject<>(-1D);
        var oldDragY = new PrimitiveObject<>(-1D);

        dragStartRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent ->
        {
            if(mouseEvent.isConsumed() || mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                oldDragX.set(-1d);
                oldDragY.set(-1d);
                return;
            }

            onDrag.run();

            oldDragX.set(mouseEvent.getSceneX());
            oldDragY.set(mouseEvent.getSceneY());
        });

        dragStartRegion.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent ->
        {
            if(mouseEvent.isConsumed()
                    || oldDragX.get() == -1d || oldDragY.get() == -1d
                    || mouseEvent.getButton() != MouseButton.PRIMARY)
            {
                return;
            }

            mouseEvent.consume();

            var dragX = mouseEvent.getSceneX();
            var dragY = mouseEvent.getSceneY();

            if(oldDragX.get() == -1D || oldDragY.get() == -1D)
            {
                oldDragX.set(dragX);
                oldDragY.set(dragY);
                return;
            }

            var newLayoutX = toDragRegion.getLayoutX() + (dragX - oldDragX.get());
            var newLayoutY = toDragRegion.getLayoutY() + (dragY - oldDragY.get());
            if(newLayoutX < 0 || (newLayoutX + toDragRegion.getWidth()) > boundRegion.getWidth() ||
                    newLayoutY < 0 || (newLayoutY + toDragRegion.getHeight()) > boundRegion.getHeight())
            {
                return;
            }

            toDragRegion.setLayoutX(newLayoutX);
            toDragRegion.setLayoutY(newLayoutY);

            oldDragX.set(dragX);
            oldDragY.set(dragY);
        });
    }

    public static void setDoubleClick(Node node, MouseButton mouseButton, Runnable runnable)
    {
        var lastClickTimestampObject = new PrimitiveObject<>(System.currentTimeMillis());
        node.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEvent ->
        {
            if(mouseEvent.isConsumed() || mouseEvent.getButton() != mouseButton)
            {
                return;
            }

            //If first time or the time has passed, refresh the timestamp
            if(System.currentTimeMillis() - lastClickTimestampObject.get() > 500)
            {
                lastClickTimestampObject.set(System.currentTimeMillis());
                return;
            }

            //If two clicks inside the 500ms of time, run thr runnable and reset the timestamp
            runnable.run();
            lastClickTimestampObject.set(System.currentTimeMillis());
        });
    }*/
}
