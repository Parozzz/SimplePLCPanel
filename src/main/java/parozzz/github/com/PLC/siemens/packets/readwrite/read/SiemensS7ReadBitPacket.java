package parozzz.github.com.PLC.siemens.packets.readwrite.read;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;

public final class SiemensS7ReadBitPacket extends SiemensS7ReadPacket
{
    public SiemensS7ReadBitPacket(SiemensS7Client client, SiemensS7PLCSocket socket)
    {
        super(client, socket, SiemensS7RWType.BIT);
    }
    
    public SiemensS7ReadBitPacket copyPacket()
    {
        return (SiemensS7ReadBitPacket) super.copyPacket();
    }
    
    public SiemensS7ReadBitPacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        return (SiemensS7ReadBitPacket) super.setAreaType(areaType, dbNumber);
    }
    
    public SiemensS7ReadBitPacket setMemoryOffset(int memoryOffset)
    {
        return (SiemensS7ReadBitPacket) super.setMemoryOffset(memoryOffset);
    }
    
    public SiemensS7ReadBitPacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        super.setNumberOfElements(1); //Always reading one single bit
        return (SiemensS7ReadBitPacket) super.sendPacket();
    }

    public boolean receiveResponse() throws SiemensS7Error.SiemensS7Exception
    {
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() >= 25)
        {
            if((isoPacketData.getLength() - 25 == 1) && (client.PDU[21] == (byte) 0xFF))
            {
                return client.PDU[25] == 1;
            }
            else
            {
                SiemensS7Error.InvalidDataRead.throwException(client, debugString);
            }
        }
        else
        {
            SiemensS7Error.ISOInvalidPDU.throwException(client);
        }
        
        return false;
    }
}
