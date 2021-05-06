package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.siemens;

import javafx.beans.property.Property;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.SiemensS7BitData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.SiemensS7StringData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.AddressStringParser;

import java.util.stream.Stream;

public final class SiemensAddressStringParser extends AddressStringParser<SiemensAddressPane>
{

    public static String createStringOfData(Data data)
    {
        var areaType = data.getAreaType();
        var readableData = data.getReadableData();

        String string = areaType.getAcronym();
        if (areaType == SiemensS7AreaType.DB)
        {
            string += data.getDbNumber() + ".DB" + readableData.getAcronym();
        }

        var byteOffset = data.getByteOffset();
        if (readableData instanceof SiemensS7BitData)
        {
            string += byteOffset + "." + data.getBitOffset();
        } else if (areaType != SiemensS7AreaType.DB)
        {
            string += readableData.getAcronym() + byteOffset;
        } else
        {
            string += byteOffset;
        }

        var extraDataParser = new ExtraDataParser();
        extraDataParser.setDataType(readableData.getName());
        if (readableData instanceof SiemensS7StringData)
        {
            extraDataParser.addData(data.getStringLength());
        }

        string += extraDataParser.parseIntoString();
        return string;
    }

    private static int parseOffset(String[] array, int index)
    {
        //If index = 3 then length should be 4 or less
        if (array.length <= index || array[index].isEmpty())
        {
            return -1;
        }

        return Integer.parseInt(array[index]);
    }

    public static Data parseDataFromString(String string)
    {
        if (string.isEmpty()) //|| string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return null;
        }
        string = string.toUpperCase();

        SiemensS7ReadableData<?> readableData;
        SiemensS7AreaType areaType;
        int dbNumber = 1;
        int byteOffset;
        int bitOffset = 0;
        int stringLength = 0;

        var extraDataParser = new ExtraDataParser(string);

        var dataType = extraDataParser.getDataType();
        if ((readableData = SiemensS7DataStorage.getFromName(dataType)) == null)
        {
            return null;
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
                        return null;
                }

                //The second character of non DB memory could be also a non letter if the data is a BIT
                var secondChar = string.charAt(1);
                if(Character.isWhitespace(secondChar))
                {
                    return null;
                }

                //The second character of non DB memory could be also a non letter if the data is a BIT
                if(readableData instanceof SiemensS7BitData)
                {
                    if(Character.isAlphabetic(secondChar))
                    {  //A bit does not have a alphabetic char after area type char
                        return null;
                    }

                    var whitespaceIndex = string.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return null;
                    }

                    var splitAddress = string.substring(1, whitespaceIndex).split("\\.");
                    if (splitAddress.length == 0)
                    {
                        return null;
                    }

                    byteOffset = parseOffset(splitAddress, 0);
                    bitOffset = parseOffset(splitAddress, 1);
                    if(byteOffset == -1 || bitOffset == -1)
                    {
                        return null;
                    }
                }
                else
                {
                    if(Character.isDigit(secondChar) || secondChar != readableData.getAcronym().charAt(0))
                    {  //A non bit always have a alphabetic char after area type char
                        return null;
                    }

                    var whitespaceIndex = string.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return null;
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
                    return null;
                }

                dbNumber = Integer.parseInt(string.substring(2, firstPointIndex));

                //Address be like DB1.DBB4 (BYTE)
                var postPointString = string.substring(firstPointIndex + 1, firstPointIndex + 3);
                if(!postPointString.equals("DB"))
                {
                    return null;
                }

                var acronym = string.substring(firstPointIndex + 3, firstPointIndex + 4);
                if(!acronym.equals(readableData.getAcronym()))
                {
                    return null;
                }

                var whitespaceIndex = string.indexOf(" ");
                if(whitespaceIndex == -1)
                {
                    return null;
                }

                //Adding 4 because after the point there are 3 more chars
                var substring = string.substring(firstPointIndex + 4, whitespaceIndex);
                //The first char in DB is always letter
                var splitAddress = substring.split("\\.");
                if (splitAddress.length == 0)
                {
                    return null;
                }

                byteOffset = parseOffset(splitAddress, 0);
                if(readableData instanceof SiemensS7BitData)
                {
                    bitOffset = parseOffset(splitAddress, 1);
                }

                if(byteOffset == -1 || bitOffset == -1)
                {
                    return null;
                }
            } else
            {
                return null;
            }

            if (readableData instanceof SiemensS7StringData)
            {
                if(extraDataParser.size() == 1)
                {
                    stringLength = extraDataParser.getIntAt(0);
                }
            }
        } catch (NumberFormatException exception)
        {
            return null;
        }

        return new Data(readableData, areaType, dbNumber, byteOffset, bitOffset, stringLength);
    }


    private boolean isUpdating;

    public SiemensAddressStringParser(AddressSetupPane<?> addressSetupPane, SiemensAddressPane addressPane)
    {
        super(addressSetupPane, addressPane);
    }

    @SuppressWarnings("unchecked")
    void init()
    {
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
        var stringAddress = this.createString();
        if (stringAddress != null)
        {
            super.setText(stringAddress);
        }
    }

    @Override
    public String createString()
    {
        var data = this.getDataFromAddressPane();
        return data == null ? null : createStringOfData(data);
    }

    private Data getDataFromAddressPane()
    {
        var readableData = addressPane.dataTypeChoiceBox.getValue();
        var areaType = addressPane.memoryAreaChoiceBox.getValue();
        if (readableData == null || areaType == null)
        {
            return null;
        }

        try
        {
            var dbNumber = Integer.parseInt(addressPane.dbTextField.getText());
            var byteOffset = Integer.parseInt(addressPane.offsetTextField.getText());
            var bitOffset = Integer.parseInt(addressPane.bitOffsetTextField.getText());
            var stringLength = Integer.parseInt(addressPane.stringLengthTextField.getText());

            return new Data(readableData, areaType, dbNumber, byteOffset, bitOffset, stringLength);
        } catch (NumberFormatException exception)
        {
            return null;
        }
    }


    @Override
    public boolean parse(String string)
    {
        var data = parseDataFromString(string);
        if(data == null)
        {
            return false;
        }

        isUpdating = true;
        addressPane.dataTypeChoiceBox.setValue(data.getReadableData());
        addressPane.memoryAreaChoiceBox.setValue(data.getAreaType());
        addressPane.dbTextField.setText("" + data.getDbNumber());
        addressPane.offsetTextField.setText("" + data.getByteOffset());
        addressPane.bitOffsetTextField.setText("" + data.getBitOffset());
        addressPane.stringLengthTextField.setText("" + data.getStringLength());
        isUpdating = false;
        return true;
    }

    public static class Data
    {
        private final SiemensS7ReadableData<?> readableData;
        private final SiemensS7AreaType areaType;
        private final int dbNumber;
        private final int byteOffset;
        private final int bitOffset;
        private final int stringLength;
        public Data(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
                int dbNumber, int byteOffset, int bitOffset, int stringLength)
        {
            this.readableData = readableData;
            this.areaType = areaType;
            this.dbNumber = dbNumber;
            this.byteOffset = byteOffset;
            this.bitOffset = bitOffset;
            this.stringLength = stringLength;
        }

        public Data(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
                int byteOffset, int bitOffset)
        {
            this(readableData, areaType, 1, byteOffset, bitOffset, 0);
        }

        public Data(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
                int dbNumber, int byteOffset, int stringLength)
        {
            this(readableData, areaType, dbNumber, byteOffset, 0, stringLength);
        }

        public SiemensS7ReadableData<?> getReadableData()
        {
            return readableData;
        }

        public SiemensS7AreaType getAreaType()
        {
            return areaType;
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
}
