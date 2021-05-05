package parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.SiemensS7BitData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;

public final class SiemensS7WritableBitWrappedDataIntermediate extends SiemensS7WrappedDataIntermediate
{
    private final SiemensS7BitData bitData;
    private final boolean value;
    public SiemensS7WritableBitWrappedDataIntermediate(SiemensS7AreaType areaType, int dbNumber,
                                                        int offset, int bitOffset, boolean value)
    {
        super(areaType, dbNumber, offset);

        this.bitData = SiemensS7DataStorage.getBit(bitOffset);
        this.value = value;
    }

    public SiemensS7BitData getS7BitData()
    {
        return bitData;
    }

    public boolean getValue()
    {
        return value;
    }
}
