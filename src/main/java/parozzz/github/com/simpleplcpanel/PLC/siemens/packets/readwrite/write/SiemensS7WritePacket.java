package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.write;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7Packet;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.SiemensS7RWType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

abstract class SiemensS7WritePacket
        extends SiemensS7Packet
{
    // S7 Read/Write Request Header (contains also ISO Header and COTP Header)
    private final byte[] WRITE_PACKET = {           // 35 bytes
            (byte) 0x03, (byte) 0x00,
            (byte) 0x00, (byte) 0x23,               // Telegram Length (Data Size 35 + All the data to write inside the PLC)
            (byte) 0x02, (byte) 0xf0, (byte) 0x80,  // COTP (see above for info)
            (byte) 0x32,                            // S7 Protocol ID
            (byte) 0x01,                            // Job Type
            (byte) 0x00, (byte) 0x00,               // Redundancy identification
            (byte) 0x05, (byte) 0x00,               // PDU Reference
            (byte) 0x00, (byte) 0x0e,               // Parameters Length
            (byte) 0x00, (byte) 0x00,               // Data Length = Size(bytes) + 4
            (byte) 0x05,                            // Function 4 Read Var, 5 Write Var
            (byte) 0x01,                            // Items count
            (byte) 0x12,                            // Var spec.
            (byte) 0x0a,                            // Length of remaining bytes
            (byte) 0x10,                            // Syntax ID
            (byte) 0x00,                            // Word Length 0x01 -> BIT, 0x02 -> BYTE
            (byte) 0x00, (byte) 0x00,               // Num Elements
            (byte) 0x00, (byte) 0x00,               // DB Number (if any, else 0)
            (byte) 0x84,                            // Memory Area Type
            (byte) 0x00, (byte) 0x00, (byte) 0x00,  // Area Offset
            // WR area
            (byte) 0x00,                            // Reserved
            (byte) 0x00,                            // Transport size. 0x03 -> BIT, 0x04 -> BYTE
            (byte) 0x00, (byte) 0x00,               // Data Length * 8 (if not timer or counter)
    };
    
    private final SiemensS7RWType type;
    private String debugString;
    protected int isoPacketSize;
    protected int elementNumber;
    
    public SiemensS7WritePacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket, SiemensS7RWType type)
    {
        super(client, plcSocket);
        
        this.type = type;
        
        WRITE_PACKET[22] = type.getWordLength();
        WRITE_PACKET[32] = type.getTransportSize();
    }
    
    public SiemensS7WritePacket copyPacket()
    {
        debugString = "Writing Data => ";

        System.arraycopy(WRITE_PACKET, 0, client.PDU, 0, WRITE_PACKET.length); //Copy Telegram
        return this;
    }
    
    protected SiemensS7WritePacket setNumberOfElements(int elementNumber)
    {
        debugString += "Element Number: " + elementNumber;

        this.isoPacketSize = WRITE_PACKET.length + elementNumber;
        SiemensS7Util.setWordAt(client.PDU, 2, isoPacketSize); //Whole Packet Size
        
        SiemensS7Util.setWordAt(client.PDU, 15, elementNumber + 4); //Number of Elements + 4
        
        this.elementNumber = elementNumber;
        SiemensS7Util.setWordAt(client.PDU, 23, elementNumber); //Set element count
        
        var length = elementNumber;
        if(type == SiemensS7RWType.BYTE)
        {
            length <<= 3; //If the type is bytes, multiply the value by 8
        }
        
        //Set the number of elements
        SiemensS7Util.setWordAt(client.PDU, 33, length); // Length
        
        return this;
    }
    
    public SiemensS7WritePacket setAreaType(SiemensS7AreaType areaType, int dbNumber)
    {
        debugString += ",AreaType: " + areaType.name() + ", DBNumber: " + dbNumber;

        if(areaType == SiemensS7AreaType.DB) //Set DB Number
        {
            SiemensS7Util.setWordAt(client.PDU, 25, dbNumber);
        }
        
        client.PDU[27] = areaType.getId(); //Set Memory Area Type
        return this;
    }

    public SiemensS7WritePacket setMemoryOffset(int memoryOffset)
    {
        debugString += ",Offset: " + memoryOffset;

        if(type == SiemensS7RWType.BYTE)
        {
            memoryOffset <<= 3; //Same as multiplying by 8 for bytes
        }
        
        // Address into the PLC (only 3 bytes)
        client.PDU[30] = (byte) (memoryOffset & 0xFF);
        memoryOffset >>= 8;
        client.PDU[29] = (byte) (memoryOffset & 0xFF);
        memoryOffset >>= 8;
        client.PDU[28] = (byte) (memoryOffset & 0xFF);
        return this;
    }

    public SiemensS7WritePacket sendPacket() throws SiemensS7Error.SiemensS7Exception
    {
        super.sendPacket(client.PDU, isoPacketSize, debugString);
        return this;
    }

    public void receiveResponse() throws SiemensS7Error.SiemensS7Exception
    {
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() == 22)
        {
            if((SiemensS7Util.getWordAt(client.PDU, 17) != 0) || (client.PDU[21] != (byte) 0xFF))
            {
                SiemensS7Error.InvalidDataWrite.throwException(client, debugString);
            }
        }
        else
        {
            SiemensS7Error.ISOInvalidPDU.throwException(client, debugString);
        }
    }
    
}
