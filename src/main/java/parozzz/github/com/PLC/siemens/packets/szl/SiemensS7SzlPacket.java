package parozzz.github.com.PLC.siemens.packets.szl;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.packets.SiemensS7Packet;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7SzlPacket
        extends SiemensS7Packet
{
    // SZL First telegram request
    private final byte[] S7_SZL_FIRST = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x21,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, (byte) 0x32,
            (byte) 0x07, (byte) 0x00, (byte) 0x00,
            (byte) 0x05, (byte) 0x00, // Sequence out
            (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x08, (byte) 0x00, (byte) 0x01, (byte) 0x12,
            (byte) 0x04, (byte) 0x11, (byte) 0x44, (byte) 0x01,
            (byte) 0x00, (byte) 0xff, (byte) 0x09, (byte) 0x00,
            (byte) 0x04,
            (byte) 0x00, (byte) 0x00, // ID (29)
            (byte) 0x00, (byte) 0x00  // Index (31)
    };
    
    // SZL Next telegram request
    private final byte[] S7_SZL_NEXT = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x21,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, (byte) 0x32,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x00,
            (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x12,
            (byte) 0x08, (byte) 0x12, (byte) 0x44, (byte) 0x01,
            (byte) 0x01, // Sequence
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    
    
    public SiemensS7SzlPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket);
    }
    
    public SiemensS7SzlData readSZL(int id, int index) throws SiemensS7Error.SiemensS7Exception
    {
        byte Seq_in = 0x00;
        int Seq_out = 0x0000;
        
        var szlData = new SiemensS7SzlData(1024);
        szlData.dataSize = 0;
        
        var first = true;
        
        var offset = 0;
        while(true)
        {
            if(first)
            {
                SiemensS7Util.setWordAt(S7_SZL_FIRST, 11, ++Seq_out);
                SiemensS7Util.setWordAt(S7_SZL_FIRST, 29, id);
                SiemensS7Util.setWordAt(S7_SZL_FIRST, 31, index);
                sendPacket(S7_SZL_FIRST, "Error on first SZL Packet");
            }
            else
            {
                SiemensS7Util.setWordAt(S7_SZL_NEXT, 11, ++Seq_out);
                client.PDU[24] = Seq_in;
                sendPacket(S7_SZL_NEXT, "Error on SZL Packet N°" + Seq_out);
            }
            
            var isoPacketData = client.receiveIsoPacket();
            if(isoPacketData.getLength() <= 32)
            {
                SiemensS7Error.FunctionError.throwException(client);
            }
            
            if(!(SiemensS7Util.getWordAt(client.PDU, 27) == 0 && client.PDU[29] == (byte) 0xFF))
            {
                SiemensS7Error.FunctionError.throwException(client);
            }
    
            int szlDataSize;
            Seq_in = client.PDU[24]; // Slice sequence
            
            // Slice sequence
            if(first)
            {
                // Gets Amount of this slice
                szlDataSize = SiemensS7Util.getWordAt(client.PDU, 31) - 8;
                szlData.lenthDR = SiemensS7Util.getWordAt(client.PDU, 37);
                szlData.n_DR = SiemensS7Util.getWordAt(client.PDU, 39);
                
                szlData.copyBuffer(client.PDU, 41, offset, szlDataSize);
            }
            else
            {
                // Gets Amount of this slice
                szlDataSize = SiemensS7Util.getWordAt(client.PDU, 31);
                
                szlData.copyBuffer(client.PDU, 37, offset, szlDataSize);
            }
            
            offset += szlDataSize;
            szlData.dataSize += szlDataSize;
            
            if(client.PDU[26] == 0x00)
            {
                return szlData;
            }
    
            first = false;
        }
    }
    
    //Returns some information about the CP (communication processor).
    public SiemensS7CommProcessorInfo getCommProcessorInfo() throws SiemensS7Error.SiemensS7Exception
    {
        var szl = readSZL(0x0131, 0x0001);
        return new SiemensS7CommProcessorInfo(szl);
    }
    
    //Returns the CPU Model Information.
    public SiemensS7ModelInfo getModelInfo() throws SiemensS7Error.SiemensS7Exception
    {
        var szl = readSZL(0x0011, 0x0000);
        return new SiemensS7ModelInfo(szl);
    }
}
