package parozzz.github.com.simpleplcpanel.util.concurrent;

public final class SettableConcurrentObject<V>
{
    private volatile V object;
    private volatile boolean objectSet = false;

    public synchronized void setObject(V object)
    {
        this.object = object;
        objectSet = true;
    }

    public synchronized boolean isObjectSet()
    {
        return objectSet;
    }

    public synchronized V getObject()
    {
        return object;
    }

    public synchronized void reset()
    {
        this.object = null;
        objectSet = false;
    }
}
