package parozzz.github.com.simpleplcpanel.util.primitiveObject;

public final class BooleanObject extends PrimitiveObject<Boolean>
{
    public BooleanObject()
    {
        super(false);
    }

    public void change(boolean value)
    {
        this.value = value;
    }

    public void set()
    {
        change(true);
    }

    public void reset()
    {
        change(false);
    }

    public void toggle()
    {
        this.value = !this.value;
    }
}
