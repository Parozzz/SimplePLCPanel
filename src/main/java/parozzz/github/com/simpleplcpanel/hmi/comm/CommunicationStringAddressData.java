package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommunicationStringAddressData
{
    private final CommunicationType<?> communicationType;
    protected String stringData = "";

    public CommunicationStringAddressData(CommunicationType<?> communicationType)
    {
        this.communicationType = communicationType;
    }

    public CommunicationType<?> getCommunicationType()
    {
        return communicationType;
    }

    public String getStringData()
    {
        return stringData;
    }

    public abstract boolean validate();

    /*
    boolean setDataToAttribute(AddressAttribute addressAttribute);

    boolean readDataFromAttribute(AddressAttribute addressAttribute);
*/

    public static class NoneStringAddressData extends CommunicationStringAddressData
    {

        public NoneStringAddressData()
        {
            super(CommunicationType.NONE);
        }

        public NoneStringAddressData(String stringData)
        {
            this();
        }

        @Override
        public boolean validate()
        {
            return true;
        }
    }

    public static class ExtraDataParser
    {
        private final List<String> extraDataList;
        private String dataType = "";

        public ExtraDataParser(String parseString)
        {
            this();

            this.parseFromString(parseString);
        }

        public ExtraDataParser()
        {
            this.extraDataList = new ArrayList<>();
        }

        public void addData(Object data)
        {
            extraDataList.add(data.toString());
        }

        public void addData(String data)
        {
            extraDataList.add(data);
        }

        public String getStringAt(int index)
        {
            return extraDataList.get(index);
        }

        public int getIntAt(int index)
        {
            return Util.parseIntOrZero(this.getStringAt(index));
        }

        public double getDoubleAt(int index)
        {
            return Util.parseDoubleOrZero(this.getStringAt(index));
        }

        public void setDataType(String dataType)
        {
            this.dataType = dataType;
        }

        public String getDataType()
        {
            return dataType;
        }

        public <E extends Enum<E>> E getDataType(Class<E> enumClass)
        {
            return Util.parseEnum(dataType, enumClass);
        }

        public boolean containsExtraData(String data)
        {
            return extraDataList.contains(data);
        }

        public int size()
        {
            return extraDataList.size();
        }

        public boolean isEmpty()
        {
            return extraDataList.isEmpty();
        }

        public void parseFromString(String parseString)
        {
            if(parseString.contains("(") && parseString.contains(")"))
            {
                dataType = parseString.substring(parseString.indexOf("(") + 1, parseString.indexOf(")"));
            }

            if(parseString.contains("[") && parseString.contains("]"))
            {
                var extraArgumentString = parseString.substring(parseString.indexOf("[") + 1, parseString.indexOf("]"));

                var splitExtraArgumentString = extraArgumentString.split(",");
                extraDataList.addAll(Arrays.asList(splitExtraArgumentString));
            }
        }

        public String parseIntoString()
        {
            var extraDataString = "";
            if(!extraDataList.isEmpty())
            {
                extraDataString += " [" + String.join(",", extraDataList) + "]";
            }

            return extraDataString + " (" + dataType + ")";
        }
    }
}
