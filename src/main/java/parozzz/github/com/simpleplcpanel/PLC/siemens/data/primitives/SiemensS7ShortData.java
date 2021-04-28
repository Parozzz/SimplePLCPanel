package parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7ShortData extends SiemensS7Data<Short>
{
    public SiemensS7ShortData()
    {
        super("INT", 2);
    }

    @Override
    public void writeBuffer(byte[] buffer, int offset, Short value)
    {
        SiemensS7Util.setShortAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "W";
    }

    @Override
    public Short readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getShortAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Short;
    }
}
