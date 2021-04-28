package parozzz.github.com.simpleplcpanel.util.primitiveObject;

public class PrimitiveObject<T>
{
    protected T value;

    public PrimitiveObject(T startValue)
    {
        this.value = startValue;
    }

    public T get()
    {
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }
}
