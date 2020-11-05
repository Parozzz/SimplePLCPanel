package parozzz.github.com.PLC.siemens.data.primitives;

import parozzz.github.com.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7FloatData extends SiemensS7Data<Float>
{
    public SiemensS7FloatData()
    {
        super("REAL", 4);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, Float value)
    {
        SiemensS7Util.setFloatAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "D";
    }

    @Override
    public Float readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getFloatAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Float;
    }
}
