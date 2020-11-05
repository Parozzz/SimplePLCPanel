package parozzz.github.com.PLC.siemens.data.primitives;

import parozzz.github.com.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7DoubleData extends SiemensS7Data<Double>
{
    public SiemensS7DoubleData()
    {
        super("LREAL", 8);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, Double value)
    {
        SiemensS7Util.setDoubleAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "X";
    }

    @Override
    public Double readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getDoubleAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Double;
    }
}
