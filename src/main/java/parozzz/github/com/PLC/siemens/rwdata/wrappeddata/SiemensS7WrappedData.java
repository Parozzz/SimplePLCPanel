package parozzz.github.com.PLC.siemens.rwdata.wrappeddata;

import parozzz.github.com.PLC.siemens.data.SiemensS7ReadableData;

abstract class SiemensS7WrappedData<V, C extends SiemensS7ReadableData<V>>
{
    protected final C data;
    private final int offset;
    protected V value;
    public SiemensS7WrappedData(C data, int offset)
    {
        this.data = data;
        this.offset = offset;
    }
    
    public abstract int getByteSize();
    
    public int getOffset()
    {
        return offset;
    }
    
    public V getValue()
    {
        return value;
    }
}
