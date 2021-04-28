package parozzz.github.com.simpleplcpanel.PLC.siemens.data;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.*;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.structure.SiemensS7DTLData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.structure.SiemensS7TimerData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

import java.util.HashMap;
import java.util.Map;

public final class SiemensS7DataStorage
{
    //1 bytes
    public final static SiemensS7BitData BIT_ZERO = create(new SiemensS7BitData(0));
    public final static SiemensS7ByteData BYTE = create(new SiemensS7ByteData());
    //2 bytes
    public final static SiemensS7WordData WORD = create(new SiemensS7WordData());
    public final static SiemensS7ShortData SHORT = create(new SiemensS7ShortData());
    //4 bytes
    public final static SiemensS7DWordData DWORD = create(new SiemensS7DWordData());
    public final static SiemensS7DIntData DINT = create(new SiemensS7DIntData());
    public final static SiemensS7FloatData FLOAT = create(new SiemensS7FloatData());
    //8 bytes
    public final static SiemensS7DoubleData DOUBLE = create(new SiemensS7DoubleData());
    //Structures
    public final static SiemensS7StringData EMPTY_STRING = create(new SiemensS7StringData(0));
    public final static SiemensS7DTLData DTL = create(new SiemensS7DTLData());
    public final static SiemensS7TimerData TIMER = create(new SiemensS7TimerData());

    private static Map<String, SiemensS7ReadableData<?>> DATA_NAME_MAP;

    //Why create a new instance every time if they are lower than 1k values? Or even 5k values
    private static final SiemensS7BitData[] BIT_DATA_ARRAY;
    private static final SiemensS7StringData[] STRING_DATA_ARRAY;
    static {
        BIT_DATA_ARRAY = new SiemensS7BitData[8];
        STRING_DATA_ARRAY = new SiemensS7StringData[255];

        BIT_DATA_ARRAY[0] = BIT_ZERO;
        for(int x = 1; x < 8; x++)
        {
            BIT_DATA_ARRAY[x] = new SiemensS7BitData(x);
        }

        //Siemens allows max a 254 chars string (Maybe 253 but just to be sure)
        STRING_DATA_ARRAY[0] = EMPTY_STRING;
        for(int x = 1; x < 255; x ++)
        {
            STRING_DATA_ARRAY[x] = new SiemensS7StringData(x);
        }
    }

    private static <T, D extends SiemensS7ReadableData<T>> D create(D data)
    {
        if(DATA_NAME_MAP == null)
        {
            DATA_NAME_MAP = new HashMap<>();
        }

        DATA_NAME_MAP.put(data.getName(), data);
        return data;
    }

    public static SiemensS7ReadableData<?> getFromName(String name)
    {
        return DATA_NAME_MAP.get(name);
    }

    public static SiemensS7BitData getBit(int bitOffset)
    {
        return BIT_DATA_ARRAY[SiemensS7Util.limitBit(bitOffset)];
    }
    
    public static SiemensS7StringData getString(int plcStringLength)
    {
        plcStringLength = Math.min(plcStringLength, 254);
        return STRING_DATA_ARRAY[plcStringLength];
    }
}
