package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.write;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;

public final class SiemensS7WriteBitPacket extends SiemensS7WritePacket
{
    public SiemensS7WriteBitPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket, SiemensS7RWType.BIT);
    }
    
    public SiemensS7WriteBitPacket copyPacket()
    {
        return (SiemensS7WriteBitPacket) super.copyPacket();
    }
    
    public SiemensS7WriteBitPacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        return (SiemensS7WriteBitPacket) super.setAreaType(areaType, dbNumber);
    }
    
    public SiemensS7WriteBitPacket setMemoryOffset(int memoryOffset)
    {
        return (SiemensS7WriteBitPacket) super.setMemoryOffset(memoryOffset);
    }
    
    public SiemensS7WriteBitPacket setBit(boolean value)
    {
        client.PDU[35] = (byte) (value ? 1 : 0);  //Set Bit status inside the data part
        return this;
    }
    
    @Override
    public SiemensS7WriteBitPacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        super.setNumberOfElements(1); //Only write 1 bit at the time
        return (SiemensS7WriteBitPacket) super.sendPacket();
    }
}
