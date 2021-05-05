package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp;

import net.wimpi.modbus.net.TCPMasterConnection;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationConnectionParams;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ModbusTCPConnectionParams extends CommunicationConnectionParams
{
    private final String ipAddress;
    private final int port;
    public ModbusTCPConnectionParams(String ipAddress, int port)
    {
        super(CommunicationType.MODBUS_TCP);

        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void updateParams(TCPMasterConnection masterConnection) throws UnknownHostException
    {
        masterConnection.setAddress(InetAddress.getByName(ipAddress));
        masterConnection.setPort(port);
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public int getPort()
    {
        return port;
    }
}
