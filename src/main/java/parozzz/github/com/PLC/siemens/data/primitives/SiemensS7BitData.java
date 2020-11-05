package parozzz.github.com.PLC.siemens.data.primitives;

import parozzz.github.com.PLC.siemens.data.SiemensS7ReadableData;

public final class SiemensS7BitData extends SiemensS7ReadableData<Boolean>
{
    private final int bitOffset;
    public SiemensS7BitData(int bitOffset)
    {
        super("BIT", 1);
        
        this.bitOffset = bitOffset;
    }

    public int getBitOffset()
    {
        return bitOffset;
    }
    
    @Override
    public Boolean readBuffer(byte[] buffer, int offset)
    {
        return (buffer[offset] & (1 << bitOffset)) != 0;
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Boolean;
    }

    @Override
    public String getAcronym()
    {
        return "X";
    }
}
