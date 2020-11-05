package parozzz.github.com.PLC.siemens.packets;

import parozzz.github.com.Nullable;
import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

import java.util.Date;

public final class SiemensS7DatePacket
        extends SiemensS7Packet
{
    // Get Date/Time request
    private final byte[] GET_DATE_PACKET = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x1d,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, (byte) 0x32,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x38,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x12,
            (byte) 0x04, (byte) 0x11, (byte) 0x47, (byte) 0x01,
            (byte) 0x00, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
            (byte) 0x00
    };
    
    // Set Date/Time command
    private final byte[] SET_DATE_PACKET = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x27,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, (byte) 0x32,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x89,
            (byte) 0x03, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x0e, (byte) 0x00, (byte) 0x01, (byte) 0x12,
            (byte) 0x04, (byte) 0x11, (byte) 0x47, (byte) 0x02,
            (byte) 0x00, (byte) 0xff, (byte) 0x09, (byte) 0x00,
            (byte) 0x0a, (byte) 0x00, (byte) 0x19, // Hi part of Year
            (byte) 0x13, // Lo part of Year
            (byte) 0x12, // Month
            (byte) 0x06, // Day
            (byte) 0x17, // Hour
            (byte) 0x37, // Min
            (byte) 0x13, // Sec
            (byte) 0x00, (byte) 0x01 // ms + Day of week
    };
    
    public SiemensS7DatePacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket);
    }
    
    @Nullable
    public Date getPlcDateTime() throws SiemensS7Error.SiemensS7Exception
    {
        sendPacket(GET_DATE_PACKET, "Error while getting PLC Date");
        
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() > 30) // the minimum expected
        {
            if((SiemensS7Util.getWordAt(client.PDU, 27) == 0) && (client.PDU[29] == (byte) 0xFF))
            {
                return SiemensS7Util.getDateAt(client.PDU, 34);
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
    
    public void setPLCDateTime(Date DateTime) throws SiemensS7Error.SiemensS7Exception
    {
        SiemensS7Util.setDateAt(SET_DATE_PACKET, 31, DateTime);
        
        super.sendPacket(SET_DATE_PACKET, "Error while setting PLC Date");
        
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() > 30) // the minimum expected
        {
            if(SiemensS7Util.getWordAt(client.PDU, 27) != 0)
            {
                SiemensS7Error.FunctionError.throwException(client);
            }
        }
        else
        {
            SiemensS7Error.ISOInvalidPDU.throwException(client);
        }
    }
    
}
