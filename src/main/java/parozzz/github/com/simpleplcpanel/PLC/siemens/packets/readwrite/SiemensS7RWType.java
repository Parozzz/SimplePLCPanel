package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite;

public enum  SiemensS7RWType
{
    BIT((byte) 0x01,(byte) 0x03),
    BYTE((byte) 0x02, (byte) 0x04);
    
    private final byte wordLength;
    private final byte transportSize;
    SiemensS7RWType(byte wordLength, byte transportSize)
    {
        this.wordLength = wordLength;
        this.transportSize = transportSize;
    }
    
    public byte getWordLength()
    {
        return wordLength;
    }
    
    public byte getTransportSize()
    {
        return transportSize;
    }
}
