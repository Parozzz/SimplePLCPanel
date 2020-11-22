package parozzz.github.com.util;

import parozzz.github.com.Main;

import java.net.URL;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util
{
    private Util()
    {
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

    public static int parseIntOrZero(String string)
    {
        return parseInt(string, 0);
    }

    public static int parseInt(String string, int errorValue)
    {
        try
        {
            return Integer.parseInt(string);
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
