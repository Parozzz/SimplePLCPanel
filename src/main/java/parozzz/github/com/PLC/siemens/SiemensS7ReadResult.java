package parozzz.github.com.PLC.siemens;

import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7ReadResult
{
    private final byte[] buffer;
    protected SiemensS7ReadResult(byte[] buffer)
    {
        this.buffer = buffer;
    }
    
    public byte[] getBuffer()
    {
        return buffer;
    }
    
    public boolean getBit(int index, int bit)
    {
        return SiemensS7Util.getBitAt(buffer, index, bit);
    }
    
    public int getWord(int index)
    {
        return SiemensS7Util.getWordAt(buffer, index);
    }
    
    public long getDWord(int index)
    {
        return SiemensS7Util.getDWordAt(buffer, index);
    }
    
    public short getShortAt(int index)
    {
        return SiemensS7Util.getShortAt(buffer, index);
    }
    
    public int getDIntAt(int index)
    {
        return SiemensS7Util.getDIntAt(buffer, index);
    }
    
    public float getFloat(int index)
    {
        return SiemensS7Util.getFloatAt(buffer, index);
    }
    
    public double getDouble(int offset)
    {
        return SiemensS7Util.getDoubleAt(buffer, offset);
    }
    
    public String getString(int index)
    {
        return SiemensS7Util.getStringAt(buffer, index);
    }
    
}
