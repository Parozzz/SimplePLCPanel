package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp;

import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPDataLength;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPFunctionCode;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;

import java.util.stream.Stream;

public class ModbusTCPAddressStringParser extends AddressStringParser<ModbusTCPAddressPane>
{

    public static String createStringFromData(Data data)
    {
        var showAddress = data.getAddress();
        boolean canHaveBit = false; //One can have bit if is ITSELF not a bit, like reading bit of holding registers!
        boolean canHaveSign = false;

        switch(data.getFunctionCode())
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

        var dataLength = data.getDataLength();

        var parseString = "" + showAddress;
        if(canHaveBit && dataLength == ModbusTCPDataLength.BIT)
        {
            parseString += "." + data.getBitNumber();
        }

        var extraDataParser = new ExtraDataParser();
        extraDataParser.setDataType(dataLength.name());
        if(canHaveSign && dataLength != ModbusTCPDataLength.BIT && data.isSigned())
        {
            extraDataParser.addData("S");
        }

        parseString += extraDataParser.parseIntoString();
        return parseString;
    }

    public static Data parseDataFromString(String string)
    {
        if(string.isEmpty()) //|| string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return null;
        }
        string = string.toUpperCase();

        var extraDataParser = new ExtraDataParser(string);

        int address;
        int bitNumber = 0;
        ModbusTCPFunctionCode functionCode;
        boolean signed = false;

        var dataLength = extraDataParser.getDataType(ModbusTCPDataLength.class);
        if(dataLength == null)
        {
            return null;
        }

        var whitespaceIndex = string.indexOf(" ");
        if(whitespaceIndex == -1)
        {
            return null;
        }

        var addressString = string.substring(0, whitespaceIndex);
        if(dataLength == ModbusTCPDataLength.BIT)
        {
            if(!addressString.contains("."))
            {
                try
                {
                    address = Integer.parseInt(addressString);
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
                    address = Integer.parseInt(splitAddressString[0]);
                    bitNumber = Integer.parseInt(splitAddressString[1]);
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
                address = Integer.parseInt(addressString);
            }
            catch(NumberFormatException exception)
            {
                return null;
            }
        }

        if(address >= 40000)
        {
            functionCode = ModbusTCPFunctionCode.HOLDING_REGISTER;
            address -= 40000;
        }else if(address >= 30000)
        {
            functionCode = ModbusTCPFunctionCode.INPUT_REGISTER;
            address -= 30000;
        }else if(address >= 10000)
        {
            functionCode = ModbusTCPFunctionCode.DISCRETE_INPUT;
            address -= 10000;
        }else
        {
            functionCode = ModbusTCPFunctionCode.COIL;
        }

        if(extraDataParser.containsExtraData("S"))
        {
            signed = true;
        }

        return new Data(functionCode, dataLength, address, bitNumber, signed);
    }

    private boolean isUpdating;

    public ModbusTCPAddressStringParser(AddressSetupPane<?> addressSetupPane, ModbusTCPAddressPane addressPane)
    {
        super(addressSetupPane, addressPane);
    }

    void init()
    {
        Stream.of(addressPane.typeChoiceBox.valueProperty(), addressPane.lengthChoiceBox.valueProperty(),
                addressPane.addressTextField.textProperty(), addressPane.bitNumberTextField.textProperty(),
                addressPane.signedCheckBox.selectedProperty())
                .forEach(property ->
                        property.addListener((observableValue, oldValue, newValue) ->
                        {
                            if(!isUpdating)
                            {
                                this.updateAddress();
                            }
                        })
                );
    }


    private void updateAddress()
    {
        var stringAddress = this.createString();
        if(stringAddress != null)
        {
            super.setText(stringAddress);
        }
    }

    @Override
    public String createString()
    {
        var data = this.getDataFromAddressPane();
        return data == null ? null : createStringFromData(data);
    }

    @Override
    public boolean parse(String string)
    {
        var data = parseDataFromString(string);
        if(data == null)
        {
            return false;
        }

        var functionCode = data.getFunctionCode();
        switch(functionCode)
        {
            case INPUT_REGISTER:
            case DISCRETE_INPUT:
                if(!addressPane.isReadAddress())
                {
                    return false;
                }
                break;
        }

        isUpdating = true;
        addressPane.typeChoiceBox.setValue(data.getFunctionCode());
        addressPane.lengthChoiceBox.setValue(data.getDataLength());
        addressPane.addressTextField.setText("" + data.getAddress());
        addressPane.signedCheckBox.setSelected(data.isSigned());
        addressPane.bitNumberTextField.setText("" + data.getBitNumber());
        isUpdating = false;

        return true;
    }


    private Data getDataFromAddressPane()
    {
        var functionCode = addressPane.typeChoiceBox.getValue();
        var dataLength = addressPane.lengthChoiceBox.getValue();
        if(functionCode == null || dataLength == null)
        {
            return null;
        }

        try
        {
            var bitNumber = Integer.parseInt(addressPane.bitNumberTextField.getText());
            var address = Integer.parseInt(addressPane.addressTextField.getText());
            var signed = addressPane.signedCheckBox.isSelected();

            return new Data(functionCode, dataLength, address, bitNumber, signed);
        }
        catch(NumberFormatException exception)
        {
            return null;
        }
    }

    public static class Data
    {
        private final ModbusTCPFunctionCode functionCode;
        private final ModbusTCPDataLength dataLength;
        private final int address;
        private final int bitNumber;
        private final boolean signed;

        public Data(ModbusTCPFunctionCode functionCode, ModbusTCPDataLength dataLength,
                int address, int bitNumber, boolean signed)
        {
            this.functionCode = functionCode;
            this.dataLength = dataLength;
            this.address = address;
            this.bitNumber = bitNumber;
            this.signed = signed;
        }

        public ModbusTCPFunctionCode getFunctionCode()
        {
            return functionCode;
        }

        public ModbusTCPDataLength getDataLength()
        {
            return dataLength;
        }

        public int getAddress()
        {
            return address;
        }

        public int getBitNumber()
        {
            return bitNumber;
        }

        public boolean isSigned()
        {
            return signed;
        }
    }
}
