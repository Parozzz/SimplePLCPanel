package parozzz.github.com.PLC.siemens.data.primitives;

import parozzz.github.com.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7DWordData extends SiemensS7Data<Long>
{
    public SiemensS7DWordData()
    {
        super("DWORD",4);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, Long value)
    {
        SiemensS7Util.setDWordAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "D";
    }

    @Override
    public Long readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getDWordAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Long;
    }
}
