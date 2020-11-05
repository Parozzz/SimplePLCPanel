package parozzz.github.com.hmi.serialize;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class SerializeUtil
{

    private final static Set<Class<?>> PRIMITIVE_NUMBER_CLASS_SET = new HashSet<>();
    static {
        PRIMITIVE_NUMBER_CLASS_SET.add(byte.class);
        PRIMITIVE_NUMBER_CLASS_SET.add(short.class);
        PRIMITIVE_NUMBER_CLASS_SET.add(int.class);
        PRIMITIVE_NUMBER_CLASS_SET.add(long.class);
        PRIMITIVE_NUMBER_CLASS_SET.add(float.class);
        PRIMITIVE_NUMBER_CLASS_SET.add(double.class);
    }

    public static boolean isPrimitiveNumber(Class<?> clazz)
    {
        return PRIMITIVE_NUMBER_CLASS_SET.contains(clazz);
    }

    public static void setPrimitiveNumber(Field field, Object object, Number number) throws IllegalAccessException
    {
        var fieldType = field.getType();

        if (byte.class.isAssignableFrom(fieldType))
        {
            field.setByte(object, number.byteValue());
        } else if (short.class.isAssignableFrom(fieldType))
        {
            field.setShort(object, number.shortValue());
        } else if (int.class.isAssignableFrom(fieldType))
        {
            field.setInt(object, number.intValue());
        } else if (long.class.isAssignableFrom(fieldType))
        {
            field.setLong(object, number.intValue());
        } else if (float.class.isAssignableFrom(fieldType))
        {
            field.setFloat(object, number.floatValue());
        } else if (double.class.isAssignableFrom(fieldType))
        {
            field.setDouble(object, number.doubleValue());
        }
    }

    private SerializeUtil()
    {

    }
}
