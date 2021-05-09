package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders;

import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.BooleanAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.NumberAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusFunctionCode;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ModbusAttributePropertyHolder
{
    /*
    public static final AttributeProperty<ModbusFunctionCode> FUNCTION_CODE = new EnumAttributeProperty<>("Modbus.FunctionCode", ModbusFunctionCode.HOLDING_REGISTER);
    public static final AttributeProperty<Integer> OFFSET = new NumberAttributeProperty<>("Modbus.Offset", 1, Number::intValue);
    public static final AttributeProperty<ModbusDataLength> DATA_LENGTH = new EnumAttributeProperty<>("Modbus.DataLength", ModbusDataLength.WORD);
    public static final AttributeProperty<Integer> BIT_OFFSET = new NumberAttributeProperty<>("Modbus.BitOffset", 0, Number::intValue);
    public static final AttributeProperty<Boolean> SIGNED = new BooleanAttributeProperty("Modbus.Signed", false);

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
        private final ModbusFunctionCode functionCode;
        private final int offset;
        private final ModbusDataLength dataLength;
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

        public ModbusFunctionCode getFunctionCode()
        {
            return functionCode;
        }

        public int getOffset()
        {
            return offset;
        }

        public ModbusDataLength getDataLength()
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
    }*/
}
