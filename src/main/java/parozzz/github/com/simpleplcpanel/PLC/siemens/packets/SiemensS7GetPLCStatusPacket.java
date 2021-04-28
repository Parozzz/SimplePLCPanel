package parozzz.github.com.simpleplcpanel.PLC.siemens.packets;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Status;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7GetPLCStatusPacket
        extends SiemensS7Packet
{
    // S7 Get PLC status (RUN/STOP/UNKNOWN)
    private static final byte[] GET_STATUS_PACKET = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x21,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, (byte) 0x32,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x2c,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x08, (byte) 0x00, (byte) 0x01, (byte) 0x12,
            (byte) 0x04, (byte) 0x11, (byte) 0x44, (byte) 0x01,
            (byte) 0x00, (byte) 0xff, (byte) 0x09, (byte) 0x00,
            (byte) 0x04, (byte) 0x04, (byte) 0x24, (byte) 0x00,
            (byte) 0x00
    };
    
    public SiemensS7GetPLCStatusPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket);
    }
    
    @Nullable
    public SiemensS7Status getStatus() throws SiemensS7Error.SiemensS7Exception
    {
        sendPacket(GET_STATUS_PACKET, "Error while getting PLC Status");
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() > 30) // the minimum expected
        {
            if(SiemensS7Util.getWordAt(client.PDU, 27) == 0)
            {
                return SiemensS7Status.getFromID(client.PDU[44]);
            }
            else
            {
                SiemensS7Error.FunctionError.throwException(client);
            }
        }
        else
        {
            SiemensS7Error.ISOInvalidPDU.throwException(client);
        }
        
        return null;
    }
}
