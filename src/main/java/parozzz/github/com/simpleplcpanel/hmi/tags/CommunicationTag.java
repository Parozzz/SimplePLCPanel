package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;

import java.util.HashSet;

public final class CommunicationTag extends Tag
{
    private final Property<CommunicationType<?>> communicationTypeProperty;
    private final Property<CommunicationStringAddressData> stringAddressDataProperty;

    public CommunicationTag(String key, CommunicationType<?> communicationType)
    {
        super(key);

        this.communicationTypeProperty = new SimpleObjectProperty<>(communicationType);
        this.stringAddressDataProperty = new SimpleObjectProperty<>(communicationType.supplyDefaultStringAddressData());

        this.communicationTypeProperty.addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null || newValue == CommunicationType.NONE)
            {
                stringAddressDataProperty.setValue(null);
                return;
            }

            stringAddressDataProperty.setValue(newValue.supplyDefaultStringAddressData());
        });
    }

    public CommunicationType<?> getCommunicationType()
    {
        return communicationTypeProperty.getValue();
    }

    public Property<CommunicationType<?>> communicationTypeProperty()
    {
        return communicationTypeProperty;
    }

    public CommunicationStringAddressData getStringAddressData()
    {
        return stringAddressDataProperty.getValue();
    }

    public Property<CommunicationStringAddressData> communicationStringAddressDataProperty()
    {
        return stringAddressDataProperty;
    }

    @Override
    public void delete()
    {
        super.delete();
    }

}
