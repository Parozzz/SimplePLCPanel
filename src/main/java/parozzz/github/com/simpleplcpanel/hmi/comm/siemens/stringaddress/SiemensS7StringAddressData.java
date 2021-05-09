package parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.SiemensS7BitData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.SiemensS7StringData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.SiemensS7AttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;

public final class SiemensS7StringAddressData
        extends CommunicationStringAddressData
{
    public static final SiemensS7ReadableData<?> DEFAULT_READABLE_DATA = SiemensS7DataStorage.BIT_ZERO;
    public static final SiemensS7AreaType DEFAULT_AREA_TYPE = SiemensS7AreaType.DB;
    public static final int DEFAULT_DB_NUMBER = 1;
    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final int DEFAULT_BIT_OFFSET = 0;
    public static final int DEFAULT_STRING_LENGTH = 1;

    @Nullable
    public static SiemensS7StringAddressData parseStringData(String stringData)
    {
        if (stringData.isEmpty() || stringData.length() < 2) //|| string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return null;
        }
        stringData = stringData.toUpperCase();

        SiemensS7ReadableData<?> readableData;
        SiemensS7AreaType areaType;
        int dbNumber = DEFAULT_DB_NUMBER;
        int byteOffset;
        int bitOffset = DEFAULT_BIT_OFFSET;
        int stringLength = DEFAULT_STRING_LENGTH;

        var extraDataParser = new CommunicationStringAddressData.ExtraDataParser(stringData);

        var dataType = extraDataParser.getDataType();
        if ((readableData = SiemensS7DataStorage.getFromName(dataType)) == null)
        {
            return null;
        }

        try
        {
            var firstChar = stringData.charAt(0);
            var secondChar = stringData.charAt(1);
            switch (firstChar)
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
                case 'D':
                    if(secondChar != 'B')
                    {
                        return null;
                    }

                    areaType = SiemensS7AreaType.DB;
                    break;
                default:
                    return null;
            }

            if (areaType == SiemensS7AreaType.OUTPUT || areaType == SiemensS7AreaType.INPUT || areaType == SiemensS7AreaType.MERKER)
            {
                //The second character of non DB memory could be also a non letter if the data is a BIT
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

                    var whitespaceIndex = stringData.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return null;
                    }

                    var splitAddress = stringData.substring(1, whitespaceIndex).split("\\.");
                    if (splitAddress.length == 0)
                    {
                        return null;
                    }

                    byteOffset = SiemensS7StringAddressData.parseOffset(splitAddress, 0);
                    bitOffset = SiemensS7StringAddressData.parseOffset(splitAddress, 1);
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

                    var whitespaceIndex = stringData.indexOf(" "); //There is always a space before the data name between parenthesis
                    if(whitespaceIndex == -1)
                    {
                        return null;
                    }

                    var substring = stringData.substring(2, whitespaceIndex);
                    byteOffset = Integer.parseInt(substring);
                }
            } else //This is a DB, no other ways
            {
                var firstPointIndex = stringData.indexOf('.');
                if(firstPointIndex == -1)
                {
                    return null;
                }

                dbNumber = Integer.parseInt(stringData.substring(2, firstPointIndex));

                //Address be like DB1.DBB4 (BYTE)
                var postPointString = stringData.substring(firstPointIndex + 1, firstPointIndex + 3);
                if(!postPointString.equals("DB"))
                {
                    return null;
                }

                var acronym = stringData.substring(firstPointIndex + 3, firstPointIndex + 4);
                if(!acronym.equals(readableData.getAcronym()))
                {
                    return null;
                }

                var whitespaceIndex = stringData.indexOf(" ");
                if(whitespaceIndex == -1)
                {
                    return null;
                }

                //Adding 4 because after the point there are 3 more chars
                var substring = stringData.substring(firstPointIndex + 4, whitespaceIndex);
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

        return new SiemensS7StringAddressData(readableData, areaType, dbNumber, byteOffset, bitOffset, stringLength);
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

    private SiemensS7ReadableData<?> readableData;
    private SiemensS7AreaType areaType;
    private int dbNumber;
    private int byteOffset;
    private int bitOffset;
    private int stringLength;

    public SiemensS7StringAddressData(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
            int dbNumber, int byteOffset, int bitOffset, int stringLength)
    {
        this.readableData = readableData;
        this.areaType = areaType;
        this.dbNumber = dbNumber;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
        this.stringLength = stringLength;

        super.stringData = this.createStringData();
    }

    public SiemensS7StringAddressData()
    {
        this(DEFAULT_READABLE_DATA, DEFAULT_AREA_TYPE, DEFAULT_DB_NUMBER,
                DEFAULT_BYTE_OFFSET, DEFAULT_BIT_OFFSET, DEFAULT_STRING_LENGTH); //This should not parse data correctly and then set default values
    }

    public SiemensS7StringAddressData(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
            int byteOffset, int bitOffset)
    {
        this(readableData, areaType, 1, byteOffset, bitOffset, 0);
    }

    public SiemensS7StringAddressData(SiemensS7ReadableData<?> readableData, SiemensS7AreaType areaType,
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

    @Nullable
    private String createStringData()
    {
        if(!this.validate())
        {
            return null;
        }

        String string = this.areaType.getAcronym();
        if (this.areaType == SiemensS7AreaType.DB)
        {
            string += this.dbNumber + ".DB" + this.readableData.getAcronym();
        }

        if (readableData instanceof SiemensS7BitData)
        {
            string += this.byteOffset + "." + this.bitOffset;
        } else if (areaType != SiemensS7AreaType.DB)
        {
            string += this.readableData.getAcronym() + this.byteOffset;
        } else
        {
            string += this.byteOffset;
        }

        var extraDataParser = new CommunicationStringAddressData.ExtraDataParser();
        extraDataParser.setDataType(this.readableData.getName());
        if (readableData instanceof SiemensS7StringData)
        {
            extraDataParser.addData(this.stringLength);
        }

        string += extraDataParser.parseIntoString();
        return string;
    }

    private void setDefaultValues()
    {
        this.readableData = SiemensS7DataStorage.BIT_ZERO;
        this.areaType = SiemensS7AreaType.DB;
        this.dbNumber = 1;
        this.byteOffset = 1;
        this.bitOffset = 0;
        this.stringLength = 1;

        super.stringData = this.createStringData();
    }

    @Override
    public boolean validate()
    {
        return !(this.readableData == null || this.areaType == null || this.dbNumber < 0
                || this.byteOffset < 0 || this.bitOffset < 0 || stringLength < 1);
    }
/*
    @Override
    public boolean setDataToAttribute(AddressAttribute addressAttribute)
    {
        if(!this.validate())
        {
            return false;
        }

        addressAttribute.setValue(AddressAttribute.SIEMENS_STRING_DATA, this);
        return true;


        addressAttribute.setValue(SiemensS7AttributePropertyHolder.S7_AREA_TYPE, this.areaType);
        addressAttribute.setValue(SiemensS7AttributePropertyHolder.S7_DATA, this.readableData);
        addressAttribute.setValue(SiemensS7AttributePropertyHolder.DB_NUMBER, this.dbNumber);
        addressAttribute.setValue(SiemensS7AttributePropertyHolder.BYTE_OFFSET, this.byteOffset);
        addressAttribute.setValue(SiemensS7AttributePropertyHolder.BIT_OFFSET, this.bitOffset);
        addressAttribute.setValue(SiemensS7AttributePropertyHolder.STRING_LENGTH, this.stringLength);
        return true;

    }

    @Override
    public boolean readDataFromAttribute(AddressAttribute addressAttribute)
    {
        var stringData = addressAttribute.getValue(AddressAttribute.STRING_DATA);
        if(stringData == null || stringData.isEmpty())
        {
            return false;
        }

        return this.parseStringData(stringData);


        var areaType = addressAttribute.getValue(SiemensS7AttributePropertyHolder.S7_AREA_TYPE);
        var readableData = addressAttribute.getValue(SiemensS7AttributePropertyHolder.S7_DATA);
        var dbNumber = addressAttribute.getValue(SiemensS7AttributePropertyHolder.DB_NUMBER);
        var byteOffset = addressAttribute.getValue(SiemensS7AttributePropertyHolder.BYTE_OFFSET);
        var bitOffset = addressAttribute.getValue(SiemensS7AttributePropertyHolder.BIT_OFFSET);
        var stringLength = addressAttribute.getValue(SiemensS7AttributePropertyHolder.STRING_LENGTH);

        if(areaType == null || readableData == null || dbNumber == null || byteOffset == null || bitOffset == null || stringLength == null)
        {
            return false;
        }

        this.areaType = areaType;
        this.readableData = readableData;
        this.dbNumber = dbNumber;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
        this.stringLength = stringLength;

        return true;
    }*/
}
