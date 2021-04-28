package parozzz.github.com.simpleplcpanel.hmi.util;

import javafx.util.StringConverter;
import parozzz.github.com.simpleplcpanel.util.Util;

public class EnumStringConverter<E extends Enum<E>> extends StringConverter<E>
{
    private final Class<E> enumClass;
    private boolean capitalize = false;
    public EnumStringConverter(Class<E> enumClass)
    {
        this.enumClass = enumClass;
    }

    public EnumStringConverter<E> setCapitalize()
    {
        this.capitalize = true;
        return this;
    }

    @Override public String toString(E anEnum)
    {
        var enumString = anEnum.name();
        if(capitalize)
        {
            enumString = Util.capitalizeWithUnderscore(enumString);
        }
        return enumString;
    }

    @Override public E fromString(String string)
    {
        return Util.parseEnum(string, enumClass);
    }
}
