package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.impl.button.ButtonWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.impl.display.DisplayWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.impl.textinput.InputWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ControlWrapperType<C extends Control, W extends ControlWrapper<C>>
{
    public static final ControlWrapperType<Button, ButtonWrapper> BUTTON = create("BUTTON_WRAPPER", "Button", Button::new, ButtonWrapper::new);
    public static final ControlWrapperType<Label, DisplayWrapper> DISPLAY = create("DISPLAY_WRAPPER", "Display", Label::new, DisplayWrapper::new);
    public static final ControlWrapperType<TextField, InputWrapper> NUMERIC_INPUT = create("NUMERIC_INPUT", "Numeric Input", TextField::new, InputWrapper::new);

    private static Map<String, ControlWrapperType<?, ?>> WRAPPER_TYPE_NAME_MAP;
    private static <T extends Control, H extends ControlWrapper<T>> ControlWrapperType<T, H> create(
            String name, String userFriendlyName, Supplier<T> controlSupplier, ControlWrapperCreator<T, H> creator)
    {
        if(WRAPPER_TYPE_NAME_MAP == null)
        {
            WRAPPER_TYPE_NAME_MAP = new HashMap<>();
        }

        var wrapperType = new ControlWrapperType<>(name, userFriendlyName, controlSupplier, creator);
        WRAPPER_TYPE_NAME_MAP.put(name, wrapperType);
        return wrapperType;
    }

    public static ControlWrapperType<?, ?> getFromName(String name)
    {
        return WRAPPER_TYPE_NAME_MAP.get(name);
    }

    private final String name;
    private final String userFriendlyName;
    private final Supplier<C> controlSupplier;
    private final ControlWrapperCreator<C, W> creator;
    private ControlWrapperType(String name, String userFriendlyName, Supplier<C> controlSupplier, ControlWrapperCreator<C, W> creator)
    {
        this.name = name;
        this.userFriendlyName = userFriendlyName;
        this.controlSupplier = controlSupplier;
        this.creator = creator;
    }

    public String getName()
    {
        return name;
    }

    public String getUserFriendlyName()
    {
        return userFriendlyName;
    }

    public C supplyControl()
    {
        return controlSupplier.get();
    }

    public W createWrapper(ControlContainerPane controlsPage)
    {
        return creator.create(controlsPage);
    }

    @FunctionalInterface
    public interface ControlWrapperCreator<C extends Control, W extends ControlWrapper<C>>
    {
        W create(ControlContainerPane mainPage);
    }
}
