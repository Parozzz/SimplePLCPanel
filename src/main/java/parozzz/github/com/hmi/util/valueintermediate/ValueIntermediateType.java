package parozzz.github.com.hmi.util.valueintermediate;

import java.util.*;
import java.util.function.Function;

public final class ValueIntermediateType<T>
{

    public static final ValueIntermediateType<Boolean> BOOLEAN = create("BOOLEAN", ValueIntermediate::asBoolean, number -> number.intValue() > 1);
    public static final ValueIntermediateType<Byte> BYTE = create("BYTE", ValueIntermediate::asByte, Number::byteValue);
    public static final ValueIntermediateType<Short> SHORT = create("SHORT", ValueIntermediate::asShort, Number::shortValue);
    public static final ValueIntermediateType<Integer> INTEGER = create("INTEGER", ValueIntermediate::asInteger, Number::intValue);
    public static final ValueIntermediateType<Long> LONG = create("LONG", ValueIntermediate::asLong, Number::longValue);
    public static final ValueIntermediateType<Float> FLOAT = create("FLOAT", ValueIntermediate::asFloat, Number::floatValue);
    public static final ValueIntermediateType<Double> DOUBLE = create("DOUBLE", ValueIntermediate::asDouble, Number::doubleValue);
    public static final ValueIntermediateType<String> HEX = create("HEX", ValueIntermediate::asStringHex, Object::toString);
    public static final ValueIntermediateType<String> STRING = create("STRING", ValueIntermediate::asString, Objects::toString);

    private static Map<String, ValueIntermediateType<?>> VALUE_NAME_MAP;
    private static List<ValueIntermediateType<?>> VALUE_LIST;
    private static <V> ValueIntermediateType<V> create(String name, Function<ValueIntermediate, V> function,
            Function<Number, V> numberToValueFunction)
    {
        var valueIntermediateType = new ValueIntermediateType<>(name, function, numberToValueFunction);

        if(VALUE_NAME_MAP == null)
        {
            VALUE_NAME_MAP = new HashMap<>();
        }

        if(VALUE_LIST == null)
        {
            VALUE_LIST = new ArrayList<>();
        }

        VALUE_LIST.add(valueIntermediateType);
        VALUE_NAME_MAP.put(name, valueIntermediateType);
        return valueIntermediateType;
    }

    public static ValueIntermediateType<?> getByName(String name)
    {
        return VALUE_NAME_MAP.get(name);
    }

    public static ValueIntermediateType<?>[] values()
    {
        return VALUE_LIST.toArray(ValueIntermediateType[]::new);
    }

    private final String name;
    private final Function<ValueIntermediate, T> function;
    private final Function<Number, T> numberToValueFunction;
    private ValueIntermediateType(String name, Function<ValueIntermediate, T> function,
            Function<Number, T> numberToValueFunction)
    {
        this.name = name;
        this.function = function;
        this.numberToValueFunction = numberToValueFunction;
    }

    public String getName()
    {
        return name;
    }

    public T getValue(ValueIntermediate valueIntermediate)
    {
        return function.apply(valueIntermediate);
    }

    public T convertNumber(Number number)
    {
        return numberToValueFunction.apply(number);
    }
}
