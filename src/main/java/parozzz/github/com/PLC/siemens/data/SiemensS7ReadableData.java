package parozzz.github.com.PLC.siemens.data;

public abstract class SiemensS7ReadableData<T>
{
    private final String name;
    private final int readByteSize;
    
    public SiemensS7ReadableData(String name, int readByteSize)
    {
        this.name = name;
        this.readByteSize = readByteSize;
    }

    public String getName()
    {
        return name;
    }
    
    public int getReadByteSize()
    {
        return readByteSize;
    }

    public abstract T readBuffer(byte[] buffer, int offset);

    public abstract boolean isSameData(Object object);

    public abstract String getAcronym();
}
