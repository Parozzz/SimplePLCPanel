package parozzz.github.com.simpleplcpanel.hmi.comm.siemens;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationConnectionParams;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;

public class SiemensS7ConnectionParams extends CommunicationConnectionParams
{
    private final String ipAddress;
    private final int rack;
    private final int slot;

    public SiemensS7ConnectionParams(String ipAddress, int rack, int slot)
    {
        super(CommunicationType.SIEMENS_S7);

        this.ipAddress = ipAddress;
        this.rack = rack;
        this.slot = slot;
    }

    public SiemensS7Client createClient()
    {
        var client = new SiemensS7Client(ipAddress, rack, slot);
        client.setConnectionTimeout(1000);
        return client;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public int getRack()
    {
        return rack;
    }

    public int getSlot()
    {
        return slot;
    }
}
