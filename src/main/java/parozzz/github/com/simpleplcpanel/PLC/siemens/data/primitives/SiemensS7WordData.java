package parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7WordData extends SiemensS7Data<Integer>
{
    public SiemensS7WordData()
    {
        super("WORD", 2);
    }
    
    @Override
    public void writeBuffer(byte[] buffer, int offset, Integer value)
    {
        SiemensS7Util.setWordAt(buffer, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "W";
    }

    @Override
    public Integer readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getWordAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Integer;
    }
}
