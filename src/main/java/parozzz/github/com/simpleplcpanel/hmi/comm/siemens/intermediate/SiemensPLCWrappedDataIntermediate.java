package parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate;

import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;

public abstract class SiemensPLCWrappedDataIntermediate
{
    private final SiemensS7AreaType s7AreaType;
    private final int dbNumber;
    private final int offset;
    public SiemensPLCWrappedDataIntermediate(SiemensS7AreaType s7AreaType, int dbNumber, int offset)
    {
        this.s7AreaType = s7AreaType;
        this.dbNumber = dbNumber;
        this.offset = offset;
    }

    public SiemensS7AreaType getAreaType()
    {
        return s7AreaType;
    }

    public int getDbNumber()
    {
        return dbNumber;
    }

    public int getOffset()
    {
        return offset;
    }
}
