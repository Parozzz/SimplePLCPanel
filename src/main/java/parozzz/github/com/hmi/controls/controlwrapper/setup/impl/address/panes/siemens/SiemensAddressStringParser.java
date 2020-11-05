package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.siemens;

import javafx.beans.property.Property;
import parozzz.github.com.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.PLC.siemens.data.primitives.SiemensS7BitData;
import parozzz.github.com.PLC.siemens.data.primitives.SiemensS7StringData;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;

import java.util.stream.Stream;

public final class SiemensAddressStringParser extends AddressStringParser<SiemensAddressPane>
{
    private boolean isUpdating;

    public SiemensAddressStringParser(SiemensAddressPane addressPane)
    {
        super(addressPane);
    }

    @SuppressWarnings("unchecked")
    void init()
    {
        var addressPane = super.getAddressPane();
        Stream.of(addressPane.memoryAreaChoiceBox.valueProperty(), addressPane.dataTypeChoiceBox.valueProperty(),
                addressPane.dbTextField.textProperty(), addressPane.offsetTextField.textProperty(),
                addressPane.bitOffsetTextField.textProperty(), addressPane.stringLengthTextField.textProperty())
                //This seems to be necessary. The compiler gives me an error otherwise. Maybe it can't mixed all the properties above?
                .map(Property.class::cast)
                .forEach(property ->
                        property.addListener((observableValue, oldValue, newValue) -> {
                            if (!isUpdating)
                            {
                                this.updateAddress();
                            }
                        })
                );
    }

    private void updateAddress()
    {
        var stringAddress = create();
        if (stringAddress != null)
        {
            super.getProperty().setValue(stringAddress);
        }
    }

    private String create()
    {
        var addressPane = super.getAddressPane();

        var readableData = addressPane.dataTypeChoiceBox.getValue();
        var areaType = addressPane.memoryAreaChoiceBox.getValue();
        int dbNumber;
        int byteOffset;
        int bitOffset;
        int stringLength;

        try
        {
            dbNumber = Integer.parseInt(addressPane.dbTextField.getText());
            byteOffset = Integer.parseInt(addressPane.offsetTextField.getText());
            bitOffset = Integer.parseInt(addressPane.bitOffsetTextField.getText());
            stringLength = Integer.parseInt(addressPane.stringLengthTextField.getText());
        } catch (NumberFormatException exception)
        {
            return null;
        }

        if (readableData == null || areaType == null)
        {
            return null;
        }

        String string = areaType.getAcronym();

        if (areaType == SiemensS7AreaType.DB)
        {
            string += dbNumber + ".DB" + readableData.getAcronym();
        }

        if (readableData instanceof SiemensS7BitData)
        {
            string += byteOffset + "." + bitOffset;
        } else if (areaType != SiemensS7AreaType.DB)
        {
            string += readableData.getAcronym() + byteOffset;
        } else
        {
            string += byteOffset;
        }

        if (readableData instanceof SiemensS7StringData)
        {
            string += " [" + stringLength + "]"; //Extra values should be inside square parenthesis and split by a comma
        }

        string += " (" + readableData.getName() + ")"; //The type should be inside a round parenthesis
        return string;
    }

    @Override
    public boolean parse(String string)
    {
        if (!super.parse(string))
        {
            return false;
        }

        string = string.toUpperCase();

        SiemensS7ReadableData<?> readableData;
        SiemensS7AreaType areaType;
        int dbNumber = 1;
        int byteOffset;
        int bitOffset = 0;
        int stringLength = 0;

        if ((readableData = SiemensS7DataStorage.getFromName(super.lastDataType)) == null)
        {
            return false;
        }

        try
        {
            if (string.startsWith("Q") || string.startsWith("I") || string.startsWith("M"))
            {
                switch (string.charAt(0))
                {
                    case 'Q':
                        areaType = SiemensS7AreaType.OUTPUT;
                        break;
                    case 'I':
                        areaType = SiemensS7AreaType.INPUT;
                        break;
                    case 'M':
                        areaType = SiemensS7AreaType.MERKER;
                        break;
                    default:
                        return false;
                }

                //The second character of non DB memory could be also a non letter if the data is a BIT
                var secondChar = string.charAt(1);
                if(Character.isWhitespace(secondChar))
                {
                    return false;
                }

                //The second character of non DB memory could be also a non letter if the data is a BIT
                if(readableData instanceof SiemensS7BitData)
                {
                    if(Character.isAlphabetic(secondChar))
                    {  //A bit does not have a alphabetic char after area type char
                        return false;
                    }

                    var whitespaceIndex = string.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return false;
                    }

                    var splitAddress = string.substring(1, whitespaceIndex).split("\\.");
                    if (splitAddress.length == 0)
                    {
                        return false;
                    }

                    byteOffset = this.parseOffset(splitAddress, 0);
                    bitOffset = this.parseOffset(splitAddress, 1);
                    if(byteOffset == -1 || bitOffset == -1)
                    {
                        return false;
                    }
                }
                else
                {
                    if(Character.isDigit(secondChar) || secondChar != readableData.getAcronym().charAt(0))
                    {  //A non bit always have a alphabetic char after area type char
                        return false;
                    }

                    var whitespaceIndex = string.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return false;
                    }

                    var substring = string.substring(2, whitespaceIndex);
                    byteOffset = Integer.parseInt(substring);
                }
            } else if (string.startsWith("DB"))
            {
                areaType = SiemensS7AreaType.DB;

                var firstPointIndex = string.indexOf('.');
                if(firstPointIndex == -1)
                {
                    return false;
                }

                dbNumber = Integer.parseInt(string.substring(2, firstPointIndex));

                //Address be like DB1.DBB4 (BYTE)
                var postPointString = string.substring(firstPointIndex + 1, firstPointIndex + 3);
                if(!postPointString.equals("DB"))
                {
                    return false;
                }

                var acronym = string.substring(firstPointIndex + 3, firstPointIndex + 4);
                if(!acronym.equals(readableData.getAcronym()))
                {
                    return false;
                }

                var whitespaceIndex = string.indexOf(" ");
                if(whitespaceIndex == -1)
                {
                    return false;
                }

                //Adding 4 because after the point there are 3 more chars
                var substring = string.substring(firstPointIndex + 4, whitespaceIndex);
                //The first char in DB is always letter
                var splitAddress = substring.split("\\.");
                if (splitAddress.length == 0)
                {
                    return false;
                }

                byteOffset = this.parseOffset(splitAddress, 0);
                if(readableData instanceof SiemensS7BitData)
                {
                    bitOffset = this.parseOffset(splitAddress, 1);
                }

                if(byteOffset == -1 || bitOffset == -1)
                {
                    return false;
                }
            } else
            {
                return false;
            }

            if (readableData instanceof SiemensS7StringData && !super.lastExtraArgumentList.isEmpty())
            {
                stringLength = Integer.parseInt(lastExtraArgumentList.get(0));
            }
        } catch (NumberFormatException exception)
        {
            return false;
        }

        isUpdating = true;
        var addressPane = super.getAddressPane();
        addressPane.dataTypeChoiceBox.setValue(readableData);
        addressPane.memoryAreaChoiceBox.setValue(areaType);
        addressPane.dbTextField.setText("" + dbNumber);
        addressPane.offsetTextField.setText("" + byteOffset);
        addressPane.bitOffsetTextField.setText("" + bitOffset);
        addressPane.stringLengthTextField.setText("" + stringLength);
        //Since i stop updating causing multiple iteration, i need to set it manually
        super.getProperty().setValue(string);
        isUpdating = false;

        return true;
    }

    private int parseOffset(String[] array, int index)
    {
        //If index = 3 then length should be 4 or less
        if (array.length <= index || array[index].isEmpty())
        {
            return -1;
        }

        return Integer.parseInt(array[index]);
    }
}
