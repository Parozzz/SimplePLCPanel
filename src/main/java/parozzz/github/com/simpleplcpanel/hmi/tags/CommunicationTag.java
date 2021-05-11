package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.*;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;

import java.util.HashSet;

public final class CommunicationTag extends Tag
{
    private final CommunicationDataHolder communicationDataHolder;
    private final BooleanProperty localProperty;
    private final Property<CommunicationStringAddressData> stringAddressDataProperty;

    public CommunicationTag(CommunicationDataHolder communicationDataHolder, String key)
    {
        super(key);

        this.communicationDataHolder = communicationDataHolder;
        this.localProperty = new SimpleBooleanProperty(false);
        this.stringAddressDataProperty = new SimpleObjectProperty<>();

        //THIS NEED TO BE REMOVE! THE LISTENER IS NEVER REMOVED AND THUS WILL KEEP THIS CLASS ALIVE FOREVER!
        //JUST FOR TESTING.
        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                stringAddressDataProperty.setValue(communicationType.supplyDefaultStringAddressData())
        );

        var communicationType = communicationDataHolder.getCurrentCommunicationType();
        if(communicationType != null)
        {
            stringAddressDataProperty.setValue(communicationType.supplyDefaultStringAddressData());
        }

        localProperty.addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null || newValue)
            {
                stringAddressDataProperty.setValue(null);
                return;
            }

            var lCommunicationType = communicationDataHolder.getCurrentCommunicationType();
            if(lCommunicationType != null)
            {
                stringAddressDataProperty.setValue(lCommunicationType.supplyDefaultStringAddressData());
            }
        });
    }

    public boolean isLocal()
    {
        return localProperty.get();
    }

    public BooleanProperty localProperty()
    {
        return localProperty;
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
