package parozzz.github.com.PLC.siemens.packets;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;

import java.io.IOException;

public abstract class SiemensS7Packet
{
    protected final SiemensS7Client client;
    private final SiemensS7PLCSocket plcSocket;
    public SiemensS7Packet(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        this.client = client;
        this.plcSocket = plcSocket;
    }

    protected void sendPacket(byte[] buffer, String exceptionInformation) throws SiemensS7Error.SiemensS7Exception
    {
        sendPacket(buffer, buffer.length, exceptionInformation);
    }

    protected void sendPacket(byte[] buffer, int length, String exceptionInformation) throws SiemensS7Error.SiemensS7Exception
    {
        try
        {
            if(!plcSocket.isConnected())
            {
                SiemensS7Error.TCPNotConnected.throwException(client);
            }
            
            var outputStream = plcSocket.getOutputStream();
            outputStream.write(buffer, 0, length);
            outputStream.flush();
        } catch (IOException ex)
        {
            SiemensS7Error.TCPDataSend.throwException(client, exceptionInformation);
        }
    }
}
