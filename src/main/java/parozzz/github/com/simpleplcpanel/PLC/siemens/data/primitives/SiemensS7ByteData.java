package parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;

public final class SiemensS7ByteData extends SiemensS7Data<Byte>
{
    public SiemensS7ByteData()
    {
        super("BYTE", 1);
    }

    @Override
    public void writeBuffer(byte[] buffer, int offset, Byte value)
    {
        buffer[offset] = value;
    }

    @Override
    public String getAcronym()
    {
        return "B";
    }

    @Override
    public Byte readBuffer(byte[] buffer, int offset)
    {
        return buffer[offset];
    }

    @Override
    public boolean isSameData(Object object)
    {
        return object instanceof Byte;
    }
}
