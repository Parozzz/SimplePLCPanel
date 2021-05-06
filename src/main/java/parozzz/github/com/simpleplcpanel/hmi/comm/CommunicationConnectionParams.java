package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.logger.Loggable;

public abstract class CommunicationConnectionParams
        implements Loggable
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
