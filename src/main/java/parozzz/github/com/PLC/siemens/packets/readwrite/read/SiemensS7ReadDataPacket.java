package parozzz.github.com.PLC.siemens.packets.readwrite.read;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;

public final class SiemensS7ReadDataPacket
        extends SiemensS7ReadPacket
{
    public SiemensS7ReadDataPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket, SiemensS7RWType.BYTE);
    }
    
    public SiemensS7ReadDataPacket copyPacket()
    {
        return (SiemensS7ReadDataPacket) super.copyPacket();
    }

    public SiemensS7ReadDataPacket setNumberOfElements(int elementNumber)
    {
        return (SiemensS7ReadDataPacket) super.setNumberOfElements(elementNumber);
    }
    
    public SiemensS7ReadDataPacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        return (SiemensS7ReadDataPacket) super.setAreaType(areaType, dbNumber);
    }
    
    public SiemensS7ReadDataPacket setMemoryOffset(int memoryOffset)
    {
        return (SiemensS7ReadDataPacket) super.setMemoryOffset(memoryOffset);
    }
    
    public SiemensS7ReadDataPacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        return (SiemensS7ReadDataPacket) super.sendPacket();
    }

    public void receiveResponse(int bufferOffset, byte[] dataBuffer) throws SiemensS7Error.SiemensS7Exception
    {
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() >= 25)
        {
            if((isoPacketData.getLength() - 25 == elementNumber) && (client.PDU[21] == (byte) 0xFF))
            {
                System.arraycopy(client.PDU, 25, dataBuffer, bufferOffset, elementNumber);
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
    }
}
