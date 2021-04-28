package parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7DIntData extends SiemensS7Data<Integer>
{
    public SiemensS7DIntData()
    {
        super("DINT", 4);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, Integer value)
    {
        SiemensS7Util.setDIntAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "D";
    }

    @Override
    public Integer readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getDIntAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Integer;
    }
}
