package parozzz.github.com.PLC.siemens.packets.readwrite.write;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;

public final class SiemensS7WriteDataPacket
        extends SiemensS7WritePacket
{
    public SiemensS7WriteDataPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket, SiemensS7RWType.BYTE);
    }
    
    public SiemensS7WriteDataPacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        return (SiemensS7WriteDataPacket) super.setAreaType(areaType, dbNumber);
    }
    
    public SiemensS7WriteDataPacket copyPacket()
    {
        return (SiemensS7WriteDataPacket) super.copyPacket();
    }
    
    public SiemensS7WriteDataPacket setNumberOfElements(int elementNumber)
    {
        return (SiemensS7WriteDataPacket) super.setNumberOfElements(elementNumber);
    }
    
    public SiemensS7WriteDataPacket setMemoryOffset(int memoryOffset)
    {
        return (SiemensS7WriteDataPacket) super.setMemoryOffset(memoryOffset);
    }

    public SiemensS7WriteDataPacket copyData(byte[] dataBuffer, int offset)
    {
        System.arraycopy(dataBuffer, offset, client.PDU, 35, elementNumber); //Copy data inside PDU starting from position 35
        return this;
    }
    
    public SiemensS7WriteDataPacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        return (SiemensS7WriteDataPacket) super.sendPacket();
    }
}
