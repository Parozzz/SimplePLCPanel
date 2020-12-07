package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.modbustcp;

import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPDataLength;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPFunctionCode;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;

import java.util.stream.Stream;

public class ModbusTCPAddressStringParser extends AddressStringParser<ModbusTCPAddressPane>
{
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
                            if (!isUpdating)
                            {
                                this.updateAddress();
                            }
                        })
                );
    }


    private void updateAddress()
    {
        var stringAddress = this.createString();
        if (stringAddress != null)
        {
            super.setText(stringAddress);
        }
    }


    @Override
    public String createString()
    {

        int address = 0; //Modbus address do start from one not zero.
        int bitNumber;

        try
        {
            bitNumber = Integer.parseInt(addressPane.bitNumberTextField.getText());
            address += Integer.parseInt(addressPane.addressTextField.getText());
        } catch (NumberFormatException exception)
        {
            return null;
        }

        boolean canHaveBit = false; //One can have bit if is ITSELF not a bit, like reading bit of holding registers!

        var functionCode = addressPane.typeChoiceBox.getValue();
        switch (functionCode)
        {
            case COIL:
                break;
            case DISCRETE_INPUT:
                address += 10000;
                break;
            case INPUT_REGISTER:
                address += 30000;
                canHaveBit = true;
                break;
            case HOLDING_REGISTER:
                address += 40000;
                canHaveBit = true;
                break;
        }

        var parseString = "" + address;

        var addressLength = addressPane.lengthChoiceBox.getValue();
        if (canHaveBit && addressLength == ModbusTCPDataLength.BIT)
        {
            parseString += "." + bitNumber;
        }

        var extraDataParser = new ExtraDataParser();
        extraDataParser.setDataType(addressLength.name());
        if (addressPane.signedCheckBox.isSelected())
        {
            extraDataParser.addData("S");
        }

        parseString += extraDataParser.parseIntoString();
        return parseString;
    }

    @Override
    public boolean parse(String string)
    {
        if (string.isEmpty()) //|| string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return false;
        }
        string = string.toUpperCase();

        var extraDataParser = new ExtraDataParser(string);

        int address;
        int bitNumber = 0;
        ModbusTCPFunctionCode functionCode;
        boolean signed = false;

        var dataLength = extraDataParser.getDataType(ModbusTCPDataLength.class);
        if (dataLength == null)
        {
            return false;
        }

        var whitespaceIndex = string.indexOf(" ");
        if(whitespaceIndex == -1)
        {
            return false;
        }

        var addressString = string.substring(0, whitespaceIndex);
        if (dataLength == ModbusTCPDataLength.BIT)
        {
            if (!addressString.contains("."))
            {
                try
                {
                    address = Integer.parseInt(addressString);
                } catch (NumberFormatException exception)
                {
                    return false;
                }
            }
            else
            {
                var splitAddressString = addressString.split("\\.");
                if (splitAddressString.length != 2)
                {
                    return false;
                }

                try
                {
                    address = Integer.parseInt(splitAddressString[0]);
                    bitNumber = Integer.parseInt(splitAddressString[1]);
                } catch (NumberFormatException exception)
                {
                    return false;
                }
            }
        } else
        {
            try
            {
                address = Integer.parseInt(addressString);
            } catch (NumberFormatException exception)
            {
                return false;
            }
        }

        if (address >= 40000)
        {
            functionCode = ModbusTCPFunctionCode.HOLDING_REGISTER;
            address -= 40000;
        } else if (address >= 30000)
        {
            if(!addressPane.isReadAddress())
            {
                return false;
            }

            functionCode = ModbusTCPFunctionCode.INPUT_REGISTER;
            address -= 30000;
        } else if (address >= 10000)
        {
            if(!addressPane.isReadAddress())
            {
                return false;
            }

            functionCode = ModbusTCPFunctionCode.DISCRETE_INPUT;
            address -= 10000;
        } else
        {
            functionCode = ModbusTCPFunctionCode.COIL;
        }

        if(extraDataParser.containsExtraData("S"))
        {
            signed = true;
        }

        isUpdating = true;
        addressPane.addressTextField.setText("" + address);
        addressPane.signedCheckBox.setSelected(signed);
        addressPane.typeChoiceBox.setValue(functionCode);
        addressPane.lengthChoiceBox.setValue(dataLength);
        addressPane.bitNumberTextField.setText("" + bitNumber);
        super.setText(string);
        isUpdating = false;

        return true;
    }
}
