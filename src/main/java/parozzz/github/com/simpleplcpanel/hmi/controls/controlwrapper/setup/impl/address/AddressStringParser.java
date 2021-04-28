package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AddressStringParser<T extends AddressPane>
{
    private final AddressSetupPane<?> addressSetupPane;
    protected final T addressPane;
    private final Property<String> textAddressProperty;

    public AddressStringParser(AddressSetupPane<?> addressSetupPane, T addressPane)
    {
        this.addressPane = addressPane;
        this.addressSetupPane = addressSetupPane;
        this.textAddressProperty = new SimpleObjectProperty<>("");
    }


    public String getStringAddress()
    {
        return textAddressProperty.getValue();
    }

    protected void setText(String text)
    {
        addressSetupPane.getTextAddressTextField().setText(text);
    }

    public abstract String createString();

    public abstract boolean parse(String string);

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

            if (parseString.contains("[") && parseString.contains("]"))
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
