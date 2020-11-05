package parozzz.github.com.util;

import parozzz.github.com.logger.MainLogger;

public final class Validate
{
    private Validate()
    {

    }

    public static void needFalse(String message, boolean... values)
    {
        needFalse(message, "", values);
    }

    public static void needFalse(String message, String append, boolean... values)
    {
        for (var value : values)
        {
            if (value)
            {
                var exception = new IllegalStateException();
                MainLogger.getInstance().warning(message + append, exception, null);
                throw exception;
            }
        }
    }

    public static void needTrue(String message, boolean... values)
    {
        needTrue(message, "", values);
    }

    public static void needTrue(String message, String append, boolean... values)
    {
        for (var value : values)
        {
            if (!value)
            {
                var exception = new IllegalStateException();
                MainLogger.getInstance().warning(message + append, exception, null);
                throw exception;
            }
        }
    }


}
