package parozzz.github.com.util.primitiveObject;

public class NumberObject<T extends Number> extends PrimitiveObject<T>
{
    public NumberObject(T startValue)
    {
        super(startValue);
    }

    public byte byteValue()
    {
        return value.byteValue();
    }

    public short shortValue()
    {
        return value.shortValue();
    }

    public int intValue()
    {
        return value.intValue();
    }

    public long longValue()
    {
        return value.longValue();
    }

    public float floatValue()
    {
        return value.floatValue();
    }

    public double doubleValue()
    {
        return value.doubleValue();
    }
}
