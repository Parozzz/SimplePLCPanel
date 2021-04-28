package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl;

public final class SiemensS7SzlData
{
    protected final byte[] dataBuffer;
    
    protected int lenthDR;
    protected int n_DR;
    protected int dataSize;
    
    protected SiemensS7SzlData(int bufferSize)
    {
        dataBuffer = new byte[bufferSize];
    }
    
    protected void copyBuffer(byte[] buffer, int sourceIndex, int destinationIndex, int size)
    {
        System.arraycopy(buffer, sourceIndex, dataBuffer, destinationIndex, size);
    }
    
    public int getLenthDR()
    {
        return lenthDR;
    }
    
    public int getN_DR()
    {
        return n_DR;
    }
    
    public int getDataSize()
    {
        return dataSize;
    }
}
