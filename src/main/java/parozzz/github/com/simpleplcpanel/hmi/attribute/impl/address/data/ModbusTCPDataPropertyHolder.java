package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data;

import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPFunctionCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ModbusTCPDataPropertyHolder
{
    public static final AttributeProperty<ModbusTCPFunctionCode> FUNCTION_CODE = new EnumAttributeProperty<>("FunctionCode", ModbusTCPFunctionCode.HOLDING_REGISTER);
    public static final AttributeProperty<Integer> OFFSET = new NumberAttributeProperty<>("Offset", 1, Number::intValue);
    public static final AttributeProperty<ModbusTCPDataLength> DATA_LENGTH = new EnumAttributeProperty<>("DataLength", ModbusTCPDataLength.WORD);
    public static final AttributeProperty<Integer> BIT_OFFSET = new NumberAttributeProperty<>("BitOffset", 0, Number::intValue);
    public static final AttributeProperty<Boolean> SIGNED = new BooleanAttributeProperty("Signed", false);

    public final static List<AttributeProperty<?>> ATTRIBUTE_PROPERTY_LIST = new ArrayList<>();

    static
    {
        ATTRIBUTE_PROPERTY_LIST.addAll(Arrays.asList(FUNCTION_CODE, OFFSET, DATA_LENGTH, BIT_OFFSET, SIGNED));
    }

    public static CachedData getCachedDataOf(AddressAttribute attribute)
    {
        return new CachedData(attribute);
    }

    public static class CachedData
    {
        private final ModbusTCPFunctionCode functionCode;
        private final int offset;
        private final ModbusTCPDataLength dataLength;
        private final boolean signed;
        private final int bitOffset;

        private CachedData(AddressAttribute attribute)
        {
            functionCode = attribute.getValue(FUNCTION_CODE);
            offset = attribute.getValue(OFFSET);
            dataLength = attribute.getValue(DATA_LENGTH);
            signed = attribute.getValue(SIGNED);
            bitOffset = attribute.getValue(BIT_OFFSET);
        }

        public ModbusTCPFunctionCode getFunctionCode()
        {
            return functionCode;
        }

        public int getOffset()
        {
            return offset;
        }

        public ModbusTCPDataLength getDataLength()
        {
            return dataLength;
        }

        public boolean isSigned()
        {
            return signed;
        }

        public int getBitOffset()
        {
            return bitOffset;
        }
    }
}
