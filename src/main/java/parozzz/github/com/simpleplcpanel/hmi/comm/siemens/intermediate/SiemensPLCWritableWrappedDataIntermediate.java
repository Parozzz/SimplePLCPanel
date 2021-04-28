package parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata.SiemensS7WritableWrappedData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;

public final class SiemensPLCWritableWrappedDataIntermediate<T> extends SiemensPLCWrappedDataIntermediate
{
    private final SiemensS7WritableWrappedData<T, ?> writableWrappedData;

    public SiemensPLCWritableWrappedDataIntermediate(SiemensS7Data<T> s7Data, SiemensS7AreaType areaType,
                                                     int dbNumber, int offset, T value)
    {
        super(areaType, dbNumber, offset);
        this.writableWrappedData = new SiemensS7WritableWrappedData<>(s7Data, offset, value);
    }

    public SiemensS7WritableWrappedData<T, ?> getS7WritableWrappedData()
    {
        return writableWrappedData;
    }
}
