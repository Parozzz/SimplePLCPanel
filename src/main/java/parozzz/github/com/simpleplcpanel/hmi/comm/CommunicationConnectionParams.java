package parozzz.github.com.simpleplcpanel.hmi.comm;

public abstract class CommunicationConnectionParams
{
    private final CommunicationType type;
    public CommunicationConnectionParams(CommunicationType type)
    {
        this.type = type;
    }

    public CommunicationType getType()
    {
        return type;
    }
}
