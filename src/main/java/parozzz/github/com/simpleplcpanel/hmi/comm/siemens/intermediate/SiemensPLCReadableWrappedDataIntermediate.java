package parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata.SiemensS7ReadableWrappedData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;

import java.util.function.Consumer;

public final class SiemensPLCReadableWrappedDataIntermediate<T> extends SiemensPLCWrappedDataIntermediate
{
    private final Consumer<T> consumer;
    private final SiemensS7ReadableWrappedData<T, ?> readableData;
    public SiemensPLCReadableWrappedDataIntermediate(SiemensS7ReadableData<T> s7Data, SiemensS7AreaType areaType,
                                                     int dbNumber, int offset, Consumer<T> consumer)
    {
        super(areaType, dbNumber, offset);

        this.consumer = consumer;
        this.readableData = new SiemensS7ReadableWrappedData<>(s7Data, offset);
    }

    public SiemensS7ReadableWrappedData<T, ?> getS7ReadableWrappedData()
    {
        return readableData;
    }

    public void parse()
    {
        var value = readableData.getValue();
        if(value != null)
        {
            consumer.accept(value);
        }
    }
}
