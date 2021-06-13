package parozzz.github.com.simpleplcpanel.util;

import parozzz.github.com.simpleplcpanel.Main;

import java.net.URL;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util
{
    private Util()
    {
    }

    public static BooleanChangeType checkChangeType(boolean newValue, boolean oldValue)
    {
        if(newValue && !oldValue)
        {
            return BooleanChangeType.RISING;
        }
        else if(!newValue && oldValue)
        {
            return BooleanChangeType.FALLING;
        }

        return BooleanChangeType.NONE;
    }

    public static String capitalizeWithUnderscore(String string)
    {
        return Stream.of(string.split("_"))
                .map(Util::capitalize)
                .collect(Collectors.joining(" "));
    }

    public static String capitalize(String string)
    {
        if(string.isEmpty())
        {
            return string;
        }
        else if(string.length() == 1)
        {
            return "" + string.charAt(0);
        }

        var capitalizedFirstLetter = ("" + string.charAt(0)).toUpperCase();
        return capitalizedFirstLetter + string.substring(1).toLowerCase();
    }

    public static <V extends Enum<V>> V parseEnum(String string, Class<V> enumClass)
    {
        try
        {
            return Enum.valueOf(enumClass, string.toUpperCase());
        } catch (IllegalArgumentException exception)
        {
            return null;
        }
    }

    public static <N extends Number> N parseNumber(String string, Function<String, N> function, N defaultValue)
    {
        try
        {
            return function.apply(string);
        }catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public static double parseDoubleOrZero(String string)
    {
        return parseDouble(string, 0);
    }

    public static double parseDouble(String string, double errorValue)
    {
        try
        {
            return Double.parseDouble(string);
        } catch (NumberFormatException exception)
        {
            return errorValue;
        }
    }

    public static byte parseByteOrZero(String string)
    {
        return parseByte(string, (byte) 0);
    }

    public static byte parseByte(String string, byte errorValue)
    {
        try
        {
            return Byte.parseByte(string);
        } catch (NumberFormatException exception)
        {
            return errorValue;
        }
    }

    public static int parseIntOrZero(String string)
    {
        return parseInt(string, 0);
    }

    public static int parseInt(String string, int errorValue)
    {
        return parseInt(string, errorValue, 10);
    }

    public static int parseInt(String string, int errorValue, int radix)
    {
        try
        {
            return Integer.parseInt(string, radix);
        } catch (NumberFormatException exception)
        {
            return errorValue;
        }
    }

    public static long parseLongOrZero(String string)
    {
        return parseLong(string, 0L);
    }

    public static long parseLong(String string, long errorValue)
    {
        try
        {
            return Long.parseLong(string);
        } catch (NumberFormatException exception)
        {
            return errorValue;
        }
    }


    public static URL getResource(String resource)
    {
        return Main.class.getResource("/" + resource);
    }

    public static String format(double number, int decimals)
    {
        return String.format("%." + decimals + "f", number);
    }
}
