package parozzz.github.com.util.number;

import parozzz.github.com.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class NumberType<N extends Number>
{
    public static final NumberType<Byte> BYTE = create(byte.class, Byte.class, Byte::parseByte,
            Number::byteValue, tByte -> Integer.toString(tByte, 16));

    public static final NumberType<Short> SHORT = create(short.class, Short.class, Short::parseShort,
            Number::shortValue, tShort -> Integer.toString(tShort, 16));

    public static final NumberType<Integer> INTEGER = create(int.class, Integer.class, Integer::parseInt,
            Number::intValue, tInt -> Integer.toString(tInt, 16));

    public static final NumberType<Long> LONG = create(long.class, Long.class, Long::parseLong,
            Number::longValue, tLong -> Long.toString(tLong, 16));

    public static final NumberType<Float> FLOAT = create(float.class, Float.class, Float::parseFloat,
            Number::floatValue, tFloat -> Integer.toString(Float.floatToIntBits(tFloat), 16));

    public static final NumberType<Double> DOUBLE = create(double.class, Double.class, Double::parseDouble,
            Number::doubleValue, tDouble -> Long.toString(Double.doubleToLongBits(tDouble), 16));

    private static Map<Class<?>, NumberType<?>> CLASS_NUMBER_TYPE_MAP;
    private static <N extends Number> NumberType<N> create(Class<?> primitiveClass, Class<N> numberClass,
            Function<String, N> parseFromString, Function<Number, N> numberToSpecific,
            Function<N, String> toHexString)
    {

        if(CLASS_NUMBER_TYPE_MAP == null)
        {
            CLASS_NUMBER_TYPE_MAP = new HashMap<>();
        }

        var numberType = new NumberType<>(primitiveClass, numberClass, parseFromString, numberToSpecific, toHexString);

        CLASS_NUMBER_TYPE_MAP.put(primitiveClass, numberType);
        CLASS_NUMBER_TYPE_MAP.put(numberClass, numberType);

        return numberType;
    }

    public static NumberType<?> getFromClass(Class<?> clazz)
    {
        return CLASS_NUMBER_TYPE_MAP.get(clazz);
    }

    private final Class<?> primitiveClass;
    private final Class<N> numberClass;
    private final Function<String, N> parseFromString;
    private final Function<Number, N> numberToSpecific;
    private final Function<N, String> toHexString;
    private NumberType(Class<?> primitiveClass, Class<N> numberClass,
            Function<String, N> parseFromString, Function<Number, N> numberToSpecific,
            Function<N, String> toHexString)
    {
        this.primitiveClass = primitiveClass;
        this.numberClass = numberClass;
        this.parseFromString = parseFromString;
        this.numberToSpecific = numberToSpecific;
        this.toHexString = toHexString;
    }

    public N fromString(String value)
    {
        return Util.parseNumber(value, parseFromString, numberToSpecific.apply(0));
    }

    public String toHexString(Number number)
    {
        return toHexString.apply(numberToSpecific.apply(number));
    }
}
