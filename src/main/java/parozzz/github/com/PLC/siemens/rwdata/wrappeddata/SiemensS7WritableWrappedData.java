package parozzz.github.com.PLC.siemens.rwdata.wrappeddata;

import parozzz.github.com.PLC.siemens.data.SiemensS7Data;

public final class SiemensS7WritableWrappedData<V, C extends SiemensS7Data<V>>
    extends SiemensS7WrappedData<V, C>
{

    public SiemensS7WritableWrappedData(C data, int offset)
    {
        super(data, offset);
    }

    public SiemensS7WritableWrappedData(C data, int offset, V value)
    {
        this(data, offset);

        this.value = value;
    }
    
    @Override
    public int getByteSize()
    {
        return data.getWriteByteSize();
    }
    
    public void writeBuffer(byte[] buffer, int offset)
    {
        data.writeBuffer(buffer, offset, value);
    }
}
