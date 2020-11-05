package parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.hmi.controls.controlwrapper.setup.impl.address.panes.AddressPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AddressStringParser<T extends AddressPane>
{
    private final T addressPane;
    private final Property<String> textAddressProperty;

    protected String lastDataType;
    protected final List<String> lastExtraArgumentList;

    public AddressStringParser(T addressPane)
    {
        this.addressPane = addressPane;

        this.textAddressProperty = new SimpleObjectProperty<>("");
        this.lastExtraArgumentList = new ArrayList<>();
    }

    public T getAddressPane()
    {
        return addressPane;
    }

    public String getStringAddress()
    {
        return textAddressProperty.getValue();
    }

    public Property<String> getProperty()
    {
        return textAddressProperty;
    }

    public boolean parse(String string)
    {
        lastExtraArgumentList.clear();

        string = string.toUpperCase();
        if (string.isEmpty() || string.length() < 4 || !string.contains("(") || !string.endsWith(")"))
        {
            return false;
        }

        lastDataType = string.substring(string.indexOf('(') + 1, string.length() - 1);
        if (string.contains("[") && string.contains("]"))
        {
            var extraArgumentString = string.substring(string.indexOf("[") + 1, string.indexOf("]"));

            var splitExtraArgumentString = extraArgumentString.split(",");
            lastExtraArgumentList.addAll(Arrays.asList(splitExtraArgumentString));
        }

        return true;
    }
}
