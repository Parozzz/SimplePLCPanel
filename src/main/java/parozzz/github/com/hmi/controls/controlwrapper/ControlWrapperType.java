package parozzz.github.com.hmi.controls.controlwrapper;

import javafx.scene.control.*;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.impl.button.ButtonWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.impl.button.ButtonWrapperValue;
import parozzz.github.com.hmi.controls.controlwrapper.impl.display.DisplayWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.impl.display.DisplayWrapperValue;
import parozzz.github.com.hmi.controls.controlwrapper.impl.textinput.InputWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.impl.textinput.InputWrapperValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ControlWrapperType<C extends Control, W extends ControlWrapper<C>>
{
    public static final ControlWrapperType<Button, ButtonWrapper> BUTTON =
            create("BUTTON_WRAPPER", "Button", Button::new, ButtonWrapper::new, ButtonWrapperValue::new);
    public static final ControlWrapperType<Label, DisplayWrapper> DISPLAY =
            create("DISPLAY_WRAPPER", "Display", Label::new, DisplayWrapper::new, DisplayWrapperValue::new);
    public static final ControlWrapperType<TextField, InputWrapper> NUMERIC_INPUT =
            create("NUMERIC_INPUT", "Numeric Input", TextField::new, InputWrapper::new, InputWrapperValue::new);

    private static Map<String, ControlWrapperType<?, ?>> WRAPPER_TYPE_NAME_MAP;
    private static <C extends Control, H extends ControlWrapper<C>> ControlWrapperType<C, H> create(
            String name, String userFriendlyName, Supplier<C> controlSupplier, ControlWrapperCreator<C, H> creator,
            BiFunction<ControlWrapper<C>, C, ControlWrapperValue<C>> wrapperValueCreator)
    {
        if(WRAPPER_TYPE_NAME_MAP == null)
        {
            WRAPPER_TYPE_NAME_MAP = new HashMap<>();
        }

        var wrapperType = new ControlWrapperType<>(name, userFriendlyName, controlSupplier, creator, wrapperValueCreator);
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
    private final BiFunction<ControlWrapper<C>, C, ControlWrapperValue<C>> wrapperValueCreator;
    private ControlWrapperType(String name, String userFriendlyName, Supplier<C> controlSupplier,
            ControlWrapperCreator<C, W> creator, BiFunction<ControlWrapper<C>, C, ControlWrapperValue<C>> wrapperValueCreator)
    {
        this.name = name;
        this.userFriendlyName = userFriendlyName;
        this.controlSupplier = controlSupplier;
        this.creator = creator;
        this.wrapperValueCreator = wrapperValueCreator;
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

    public ControlWrapperValue<C> createWrapperValue(ControlWrapper<C> controlWrapper, C control)
    {
        return wrapperValueCreator.apply(controlWrapper, control);
    }

    @FunctionalInterface
    public interface ControlWrapperCreator<C extends Control, W extends ControlWrapper<C>>
    {
        W create(ControlContainerPane mainPage);
    }
}
