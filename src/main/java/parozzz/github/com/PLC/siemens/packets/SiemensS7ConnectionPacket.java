package parozzz.github.com.PLC.siemens.packets;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.SiemensS7PLCSocket;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7ConnectionPacket
        extends SiemensS7Packet
{
    // ISO Connection Request telegram (contains also ISO Header and COTP Header)
    private final byte[] CONNECTION_REQUEST_PACKET = {
            // TPKT (RFC1006 Header)
            (byte) 0x03, // RFC 1006 ID (3)
            (byte) 0x00, // Reserved, always 0
            (byte) 0x00, // High part of packet length (entire frame, payload and TPDU included)
            (byte) 0x16, // Low part of packet length (entire frame, payload and TPDU included)
            // COTP (ISO 8073 Header)
            (byte) 0x11, // PDU Size Length
            (byte) 0xE0, // CR - Connection Request ID
            (byte) 0x00, // Dst Reference HI
            (byte) 0x00, // Dst Reference LO
            (byte) 0x00, // Src Reference HI
            (byte) 0x01, // Src Reference LO
            (byte) 0x00, // Class + Options Flags
            (byte) 0xC0, // PDU Max Length ID
            (byte) 0x01, // PDU Max Length HI
            (byte) 0x0A, // PDU Max Length LO
            (byte) 0xC1, // Src TSAP Identifier
            (byte) 0x02, // Src TSAP Length (2 bytes)
            (byte) 0x01, // Src TSAP HI (will be overwritten)
            (byte) 0x00, // Src TSAP LO (will be overwritten)
            (byte) 0xC2, // Dst TSAP Identifier
            (byte) 0x02, // Dst TSAP Length (2 bytes)
            (byte) 0x01, // Dst TSAP HI (will be overwritten)
            (byte) 0x02  // Dst TSAP LO (will be overwritten)
    };
    
    private SiemensS7Util.ConnectionType connectionType;
    private int rack;
    private int slot;
    
    public SiemensS7ConnectionPacket(SiemensS7Client client, SiemensS7PLCSocket plcSocket)
    {
        super(client, plcSocket);
    }
    
    public SiemensS7ConnectionPacket setConnectionType(SiemensS7Util.ConnectionType connectionType)
    {
        this.connectionType = connectionType;
        return this;
    }
    
    public SiemensS7ConnectionPacket setPLCPosition(int rack, int slot)
    {
        this.rack = rack;
        this.slot = slot;
        return this;
    }
    
    public void connect() throws SiemensS7Error.SiemensS7Exception
    {
        var localTSAP = 0x0100;
        var remoteTSAP = ((connectionType.getId() << 8) + (rack * 0x20) + slot) & 0xFFFF;
        
        CONNECTION_REQUEST_PACKET[16] = (byte) (localTSAP >> 8); //LocalTSAP_HIGH
        CONNECTION_REQUEST_PACKET[17] = (byte) (localTSAP & 0xFF); //LocalTSAP_LOW
        
        CONNECTION_REQUEST_PACKET[20] = (byte) (remoteTSAP >> 8); //RemoteTSAP_HIGH
        CONNECTION_REQUEST_PACKET[21] = (byte) (remoteTSAP & 0xFF); //RemoteTSAP_LOW
        
        // Sends the connection request telegram
        sendPacket(CONNECTION_REQUEST_PACKET, "Error while sending connection packet");
        
        // Gets the reply (if any). Cannot return null it would throw an exception otherwise.
        var isoPacketData = client.receiveIsoPacket();
        if(isoPacketData.getLength() == 22)
        {
            if(!isoPacketData.isConnectionEstablished())
            {
                SiemensS7Error.ISOConnectionFailed.throwException(client);
            }
        }
        else
        {
            SiemensS7Error.ISOInvalidPDU.throwException(client);
        }
    }
/*
    private void isoConnect(int remoteTSAP)
    {
        var localTSAP = 0x0100;
        remoteTSAP = remoteTSAP & 0xFFFF;

        CONNECTION_REQUEST_PACKET[16] = (byte) (localTSAP >> 8); //LocalTSAP_HIGH
        CONNECTION_REQUEST_PACKET[17] = (byte) (localTSAP & 0xFF); //LocalTSAP_LOW

        CONNECTION_REQUEST_PACKET[20] = (byte) (remoteTSAP >> 8); //RemoteTSAP_HIGH
        CONNECTION_REQUEST_PACKET[21] = (byte) (remoteTSAP & 0xFF); //RemoteTSAP_LOW

        // Sends the connection request telegram
        sendPacket(CONNECTION_REQUEST_PACKET);
        if (lastError == 0)
        {
            // Gets the reply (if any)
            var isoPacketData = receiveIsoPacket();
            if (isoPacketData != null && lastError == 0)
            {
                if (isoPacketData.getLength() == 22)
                {
                    if (!isoPacketData.isConnectionEstablished())
                    {
                        lastError = errISOConnectionFailed;
                    }
                } else
                {
                    lastError = errISOInvalidPDU;
                }
            }
        }
    }*/
    
}

