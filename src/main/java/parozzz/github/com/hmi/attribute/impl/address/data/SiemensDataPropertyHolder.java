package parozzz.github.com.hmi.attribute.impl.address.data;

import parozzz.github.com.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.hmi.attribute.property.impl.primitives.NumberAttributeProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SiemensDataPropertyHolder
{
    public static final AttributeProperty<SiemensS7ReadableData<?>> S7_DATA = new FunctionAttributeProperty<>("S7Data", SiemensS7DataStorage.BIT_ZERO,
            SiemensS7ReadableData::getName,
            (jsonDataMap, key) -> SiemensS7DataStorage.getFromName(jsonDataMap.getString(key)));
    public static final AttributeProperty<SiemensS7AreaType> S7_AREA_TYPE = new EnumAttributeProperty<>("S7AreaType", SiemensS7AreaType.DB);
    public static final AttributeProperty<Integer> DB_NUMBER = new NumberAttributeProperty<>("DBNumber", 1, Number::intValue);
    public static final AttributeProperty<Integer> BYTE_OFFSET = new NumberAttributeProperty<>("ByteOffset", 0, Number::intValue);
    public static final AttributeProperty<Integer> BIT_OFFSET = new NumberAttributeProperty<>("BitOffset", 0,Number::intValue);
    public static final AttributeProperty<Integer> STRING_LENGTH = new NumberAttributeProperty<>("StringLength", 1, Number::intValue);

    public final static List<AttributeProperty<?>> ATTRIBUTE_PROPERTY_LIST = new ArrayList<>();
    static
    {
        ATTRIBUTE_PROPERTY_LIST.addAll(Arrays.asList(S7_DATA, S7_AREA_TYPE, DB_NUMBER, BYTE_OFFSET, BIT_OFFSET, STRING_LENGTH));
    }

    public static CachedData getCachedDataOf(AddressAttribute attribute)
    {
        return new CachedData(attribute);
    }

    public static class CachedData
    {
        private final SiemensS7ReadableData<?> s7Data;
        private final SiemensS7AreaType s7AreaType;
        private final int dbNumber;
        private final int byteOffset;
        private final int bitOffset;
        private final int stringLength;
        private CachedData(AddressAttribute attribute)
        {
            s7Data = attribute.getValue(S7_DATA);
            s7AreaType = attribute.getValue(S7_AREA_TYPE);
            dbNumber = attribute.getValue(DB_NUMBER);
            byteOffset = attribute.getValue(BYTE_OFFSET);
            bitOffset = attribute.getValue(BIT_OFFSET);
            stringLength = attribute.getValue(STRING_LENGTH);
        }

        public SiemensS7ReadableData<?> getS7Data()
        {
            return s7Data;
        }

        public SiemensS7AreaType getS7AreaType()
        {
            return s7AreaType;
        }

        public int getDbNumber()
        {
            return dbNumber;
        }

        public int getByteOffset()
        {
            return byteOffset;
        }

        public int getBitOffset()
        {
            return bitOffset;
        }

        public int getStringLength()
        {
            return stringLength;
        }
    }

    private SiemensDataPropertyHolder() {}
}
