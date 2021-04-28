package parozzz.github.com.simpleplcpanel.PLC.siemens.data;

public abstract class SiemensS7Data<T> extends SiemensS7ReadableData<T>
{
    private final int writeByteSize;
    public SiemensS7Data(String name, int byteSize)
    {
        this(name, byteSize, byteSize);
    }
    
    public SiemensS7Data(String name, int readByteSize, int writeByteSize)
    {
        super(name, readByteSize);
        
        this.writeByteSize = writeByteSize;
    }
    
    public int getWriteByteSize()
    {
        return writeByteSize;
    }
    
    public abstract void writeBuffer(byte[] buffer, int offset, T value);
}
