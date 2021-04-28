package parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;

public final class SiemensS7ReadableWrappedData<V, C extends SiemensS7ReadableData<V>>
    extends SiemensS7WrappedData<V, C>
{
    public SiemensS7ReadableWrappedData(C data, int offset)
    {
        super(data, offset);
    }
    
    @Override
    public int getByteSize()
    {
        return data.getReadByteSize();
    }
    
    public void readBuffer(byte[] buffer, int offset)
    {
        value = data.readBuffer(buffer, offset);
    }
}
