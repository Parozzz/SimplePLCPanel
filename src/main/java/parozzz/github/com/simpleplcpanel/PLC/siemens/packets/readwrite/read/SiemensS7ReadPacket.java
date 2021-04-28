package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.read;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7Packet;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

abstract class SiemensS7ReadPacket extends SiemensS7Packet
{
    // S7 Read/Write Request Header (contains also ISO Header and COTP Header)
    private final byte[] READ_PACKET = {           // 31 bytes
            (byte) 0x03, (byte) 0x00,               // B0 B1,
            (byte) 0x00, (byte) 0x1F,               // B2 B3, Telegram Length (Data Size 31 bytes)
            (byte) 0x02, (byte) 0xf0, (byte) 0x80,  // B4 B5 B6, COTP (see above for info)
            (byte) 0x32,                            // B7, S7 Protocol ID
            (byte) 0x01,                            // B8, Job Type
            (byte) 0x00, (byte) 0x00,               // B9 B10, Redundancy identification
            (byte) 0x05, (byte) 0x00,               // B11 B12, PDU Reference
            (byte) 0x00, (byte) 0x0e,               // B13 B14, Parameters Length
            (byte) 0x00, (byte) 0x00,               // B15 B16, Data Length = Size(bytes) + 4
            (byte) 0x04,                            // B17, Function 4 Read Var, 5 Write Var
            (byte) 0x01,                            // B18, Items count
            (byte) 0x12,                            // B19, Var spec.
            (byte) 0x0a,                            // B20, Length of remaining bytes
            (byte) 0x10,                            // B21, Syntax ID
            (byte) 0x00,                            // B22, World Length 0x01 -> BIT, 0x02 -> BYTE
            (byte) 0x00, (byte) 0x00,               // B23 B24, Num Elements
            (byte) 0x00, (byte) 0x00,               // B25 B26, DB Number (if any, else 0)
            (byte) 0x00,                            // B27, Memory Area Type
            (byte) 0x00, (byte) 0x00, (byte) 0x00,  // B28 B29 B30, Area Offset
    };
    
    private final SiemensS7RWType type;
    protected String debugString;
    protected int elementNumber;
    public SiemensS7ReadPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket, SiemensS7RWType type)
    {
        super(client, plcSocket);
        
        this.type = type;
    
        READ_PACKET[22] = type.getWordLength();
    }
    
    public SiemensS7ReadPacket copyPacket()
    {
        debugString = "Reading Data => ";
        System.arraycopy(READ_PACKET, 0, client.PDU, 0, READ_PACKET.length); //Copy Telegram
        return this;
    }
    
    protected SiemensS7ReadPacket setNumberOfElements(int elementNumber)
    {
        debugString += "Element Number: " + elementNumber;

        this.elementNumber = elementNumber;
        SiemensS7Util.setWordAt(client.PDU, 23, elementNumber); //Set element count
        return this;
    }
    
    public SiemensS7ReadPacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        debugString += ",AreaType: " + areaType.name() + ", DBNumber: " + dbNumber;

        if(areaType == SiemensS7AreaType.DB) //Set DB Number
        {
            SiemensS7Util.setWordAt(client.PDU, 25, dbNumber);
        }
        
        client.PDU[27] = areaType.getId(); //Set Memory Area Type
        return this;
    }
    
    public SiemensS7ReadPacket setMemoryOffset(int memoryOffset)
    {
        debugString += ",Offset: " + memoryOffset;

        if(type == SiemensS7RWType.BYTE)
        {
            memoryOffset <<= 3; //Multiply by 8 for bytes
        }
        
        // Address into the PLC (only 3 bytes)
        client.PDU[30] = (byte) (memoryOffset & 0xFF);
        memoryOffset >>= 8;
        client.PDU[29] = (byte) (memoryOffset & 0xFF);
        memoryOffset >>= 8;
        client.PDU[28] = (byte) (memoryOffset & 0xFF);
        
        return this;
    }
    
    public SiemensS7ReadPacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        super.sendPacket(client.PDU, READ_PACKET.length, debugString);
        return this;
    }
}
