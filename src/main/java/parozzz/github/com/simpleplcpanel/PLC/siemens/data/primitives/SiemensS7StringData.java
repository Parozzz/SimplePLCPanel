package parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7StringData extends SiemensS7Data<String>
{
    private static int getPLCStringByteSize(int plcStringLength)
    {
        if(plcStringLength % 2 != 0)
        {
            plcStringLength ++;
        }
        
        return plcStringLength + 2;
    }
    
    private final int plcStringLength;
    public SiemensS7StringData(int plcStringLength)
    {
        super("STRING", SiemensS7StringData.getPLCStringByteSize(plcStringLength));
        
        this.plcStringLength = plcStringLength;
    }

    @Override
    public void writeBuffer(byte[] buffer, int offset, String value)
    {
        SiemensS7Util.setStringAt(buffer, plcStringLength, offset, value);
    }

    @Override
    public String getAcronym()
    {
        return "X";
    }

    @Override
    public String readBuffer(byte[] buffer, int offset)
    {
        return SiemensS7Util.getStringAt(buffer, offset);
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof String;
    }
}
