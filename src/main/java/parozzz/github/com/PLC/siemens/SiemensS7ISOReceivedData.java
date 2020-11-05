package parozzz.github.com.PLC.siemens;

public final class SiemensS7ISOReceivedData
{
    private final int length;
    private final boolean connectionEstablished;
    public SiemensS7ISOReceivedData(int length, boolean connectionEstablished)
    {
        this.length = length;
        this.connectionEstablished = connectionEstablished;
    }

    public int getLength()
    {
        return length;
    }

    public boolean isConnectionEstablished()
    {
        return connectionEstablished;
    }
}
