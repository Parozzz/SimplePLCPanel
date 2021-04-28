package parozzz.github.com.simpleplcpanel.PLC.siemens.packets;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7NegotiatePDULengthPacket
        extends SiemensS7Packet
{
    private static final int DefaultPduSizeRequested = 480;
    
    // S7 PDU Negotiation Telegram (contains also ISO Header and COTP Header)
    //25 bytes
    private final byte[] PDQ_NEGOTIATION_PACKET = {
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x19,
            (byte) 0x02, (byte) 0xf0, (byte) 0x80, // TPKT + COTP (see above for info)
            (byte) 0x32, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x08,
            (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
            (byte) (DefaultPduSizeRequested >> 8), (byte) (DefaultPduSizeRequested & 0xFF) // PDU Length Requested = HI-LO 480 bytes
    };
    
    public SiemensS7NegotiatePDULengthPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket);
    }
    
    public int exchangePDULength() throws SiemensS7Error.SiemensS7Exception
    {
        super.sendPacket(PDQ_NEGOTIATION_PACKET, "Error while negotiating PDU length");
        
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() == 27 && client.PDU[17] == 0 && client.PDU[18] == 0)  // 20 = size of Negotiate Answer
        {
            // Get PDU Size Negotiated
            var pduLength = SiemensS7Util.getWordAt(client.PDU, 25);
            if(pduLength > 0)
            {
                return pduLength;
            }
            else
            {
                SiemensS7Error.ISONegotiationPDU.throwException(client);
            }
        }
        else
        {
            SiemensS7Error.ISONegotiationPDU.throwException(client);
        }
        
        return -1;
    }
}
