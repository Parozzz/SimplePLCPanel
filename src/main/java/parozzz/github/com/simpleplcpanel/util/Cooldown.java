package parozzz.github.com.simpleplcpanel.util;

public final class Cooldown
{
    private final long timeout;
    private long timestamp = -1;
    public Cooldown(long timeout)
    {
        this.timeout = timeout;
    }

    public void createStamp()
    {
        timestamp = System.currentTimeMillis();
    }

    public boolean passed()
    {
        if(timestamp == -1 || (System.currentTimeMillis() - timestamp) > timeout)
        {
            timestamp = System.currentTimeMillis();
            return true;
        }

        return false;
    }
}
