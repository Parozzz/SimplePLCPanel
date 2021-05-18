package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.ModbusAttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusDataLength;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.ModbusFunctionCode;

public class ModbusStringAddressData extends CommunicationStringAddressData
{
    public static final ModbusFunctionCode DEFAULT_FUNCTION_CODE = ModbusFunctionCode.HOLDING_REGISTER;
    public static final ModbusDataLength DEFAULT_DATA_LENGTH = ModbusDataLength.WORD;
    public static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_BIT_OFFSET = 0;
    public static final boolean DEFAULT_SIGNED = false;

    @Nullable
    public static ModbusStringAddressData parseStringData(String stringData)
    {
        if(stringData == null || stringData.isEmpty() || stringData.length() < 5) //|| string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return null;
        }
        stringData = stringData.toUpperCase();

        var extraDataParser = new CommunicationStringAddressData.ExtraDataParser(stringData);

        int offset;
        int bitOffset = 0;
        ModbusFunctionCode functionCode;
        var signed = extraDataParser.containsExtraData("S");

        var dataLength = extraDataParser.getDataType(ModbusDataLength.class);
        if(dataLength == null)
        {
            return null;
        }

        var whitespaceIndex = stringData.indexOf(" ");
        if(whitespaceIndex == -1)
        {
            return null;
        }

        var addressString = stringData.substring(0, whitespaceIndex);
        if(dataLength == ModbusDataLength.BIT)
        {
            if(!addressString.contains("."))
            {
                try
                {
                    offset = Integer.parseInt(addressString);
                }
                catch(NumberFormatException exception)
                {
                    return null;
                }
            }else
            {
                var splitAddressString = addressString.split("\\.");
                if(splitAddressString.length != 2)
                {
                    return null;
                }

                try
                {
                    offset = Integer.parseInt(splitAddressString[0]);
                    bitOffset = Integer.parseInt(splitAddressString[1]);
                }
                catch(NumberFormatException exception)
                {
                    return null;
                }
            }
        }else
        {
            try
            {
                offset = Integer.parseInt(addressString);
            }
            catch(NumberFormatException exception)
            {
                return null;
            }
        }

        if(offset >= 40000)
        {
            functionCode = ModbusFunctionCode.HOLDING_REGISTER;
            offset -= 40000;
        }else if(offset >= 30000)
        {
            functionCode = ModbusFunctionCode.INPUT_REGISTER;
            offset -= 30000;
        }else if(offset >= 10000)
        {
            functionCode = ModbusFunctionCode.DISCRETE_INPUT;
            offset -= 10000;
        }else
        {
            functionCode = ModbusFunctionCode.COIL;
        }

        return new ModbusStringAddressData(functionCode, dataLength, offset, bitOffset, signed);
    }

    private final ModbusFunctionCode functionCode;
    private final ModbusDataLength dataLength;
    private final int offset;
    private final int bitOffset;
    private final boolean signed;

    public ModbusStringAddressData(ModbusFunctionCode functionCode, ModbusDataLength dataLength,
            int offset, int bitOffset, boolean signed)
    {
        super(CommunicationType.MODBUS_TCP);

        this.functionCode = functionCode;
        this.dataLength = dataLength;
        this.offset = offset;
        this.bitOffset = bitOffset;
        this.signed = signed;

        super.stringData = this.createStringData();
    }

    public ModbusStringAddressData()
    {
        this(DEFAULT_FUNCTION_CODE, DEFAULT_DATA_LENGTH, DEFAULT_OFFSET,
                DEFAULT_BIT_OFFSET, DEFAULT_SIGNED);
    }

    public ModbusFunctionCode getFunctionCode()
    {
        return functionCode;
    }

    public ModbusDataLength getDataLength()
    {
        return dataLength;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getBitOffset()
    {
        return bitOffset;
    }

    public boolean isSigned()
    {
        return signed;
    }

    @Override
    public boolean validate()
    {
        return !(this.functionCode == null || this.dataLength == null || offset < 0 || bitOffset < 0);
    }

    @Nullable
    private String createStringData()
    {
        if(!validate())
        {
            return null;
        }

        var showAddress = this.offset;

        boolean canHaveBit = false; //One can have bit if is ITSELF not a bit, like reading bit of holding registers!
        boolean canHaveSign = false;

        switch (this.functionCode)
        {
            case COIL:
                break;
            case DISCRETE_INPUT:
                showAddress += 10000;
                break;
            case INPUT_REGISTER:
                showAddress += 30000;
                canHaveBit = true;
                canHaveSign = true;
                break;
            case HOLDING_REGISTER:
                showAddress += 40000;
                canHaveBit = true;
                canHaveSign = true;
                break;
        }

        var parseString = "" + showAddress;
        if (canHaveBit && this.dataLength == ModbusDataLength.BIT)
        {
            parseString += "." + this.bitOffset;
        }

        var extraDataParser = new CommunicationStringAddressData.ExtraDataParser();
        extraDataParser.setDataType(dataLength.name());
        if (canHaveSign && dataLength != ModbusDataLength.BIT && this.signed)
        {
            extraDataParser.addData("S");
        }

        parseString += extraDataParser.parseIntoString();
        return parseString;
    }

    /*
    @Override
    public boolean setDataToAttribute(AddressAttribute addressAttribute)
    {
        if(!this.validate())
        {
            return false;
        }

        var stringData = this.createStringData();
        if(stringData != null)
        {
            addressAttribute.setValue(AddressAttribute.STRING_DATA, stringData);
            return true;
        }

        return false;

        addressAttribute.setValue(ModbusAttributePropertyHolder.FUNCTION_CODE, this.functionCode);
        addressAttribute.setValue(ModbusAttributePropertyHolder.OFFSET, this.offset);
        addressAttribute.setValue(ModbusAttributePropertyHolder.DATA_LENGTH, this.dataLength);
        addressAttribute.setValue(ModbusAttributePropertyHolder.BIT_OFFSET, this.bitOffset);
        addressAttribute.setValue(ModbusAttributePropertyHolder.SIGNED, this.signed);
        return true;
    }*/

    /*
    @Override
    public boolean readDataFromAttribute(AddressAttribute addressAttribute)
    {
        var stringData = addressAttribute.getValue(AddressAttribute.STRING_DATA);
        if(stringData == null || stringData.isEmpty())
        {
            return false;
        }

        return this.parseStringData(stringData);

        var functionCode = addressAttribute.getValue(ModbusAttributePropertyHolder.FUNCTION_CODE);
        var offset = addressAttribute.getValue(ModbusAttributePropertyHolder.OFFSET);
        var dataLength = addressAttribute.getValue(ModbusAttributePropertyHolder.DATA_LENGTH);
        var bitOffset = addressAttribute.getValue(ModbusAttributePropertyHolder.BIT_OFFSET);
        var signed = addressAttribute.getValue(ModbusAttributePropertyHolder.SIGNED);

        if(functionCode == null || offset == null || dataLength == null || bitOffset == null || signed == null)
        {
            return false;
        }

        this.functionCode = functionCode;
        this.offset = offset;
        this.dataLength = dataLength;
        this.bitOffset = bitOffset;
        this.signed = signed;
        return true;
    }*/
}
