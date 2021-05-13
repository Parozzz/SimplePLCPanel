package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.*;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CommunicationTag extends Tag
{
    private final BooleanProperty localProperty;
    private final Property<CommunicationStringAddressData> stringAddressDataProperty;

    private final Map<CommunicationType<?>, Property<CommunicationStringAddressData>> stringAddressDataMap;
    private final Set<WeakReference<Taggable>> weakTaggableSet;

    private Property<CommunicationStringAddressData> selectedStringAddressDataProperty;

    public CommunicationTag(String key)
    {
        super(key);

        this.localProperty = new SimpleBooleanProperty(false);
        this.stringAddressDataProperty = new SimpleObjectProperty<>();

        this.stringAddressDataMap = new HashMap<>();
        this.weakTaggableSet = new HashSet<>();

        for (var communicationType : CommunicationType.values())
        {
            var stringAddressData = communicationType.supplyDefaultStringAddressData();
            if (stringAddressData != null)
            {
                var property = new SimpleObjectProperty<>(stringAddressData);
                stringAddressDataMap.put(communicationType, property);
            }

        }

        localProperty.addListener((observable, oldValue, newValue) ->
        {
            if (newValue == null || newValue)
            {
                this.updateSelectedStringAddressDataProperty(selectedStringAddressDataProperty, null);
                return;
            }

            this.updateSelectedStringAddressDataProperty(null, selectedStringAddressDataProperty);
        });
    }

    public void updateCommunicationType(CommunicationType<?> communicationType)
    {
        var oldSelectedProperty = this.selectedStringAddressDataProperty;
        selectedStringAddressDataProperty = stringAddressDataMap.get(communicationType);

        this.updateSelectedStringAddressDataProperty(oldSelectedProperty, selectedStringAddressDataProperty);
    }

    private void updateSelectedStringAddressDataProperty(Property<CommunicationStringAddressData> oldValue,
            Property<CommunicationStringAddressData> newValue)
    {
        if (oldValue != null)
        {
            oldValue.unbind();
        }

        if (localProperty.get())
        {
            stringAddressDataProperty.setValue(null);
            return;
        }

        if (newValue != null)
        {
            stringAddressDataProperty.setValue(newValue.getValue());
            newValue.bind(stringAddressDataProperty);
        }
    }

    public boolean isLocal()
    {
        return localProperty.get();
    }

    public BooleanProperty localProperty()
    {
        return localProperty;
    }

    public void setStringAddressData(CommunicationStringAddressData stringAddressData)
    {
        stringAddressDataProperty.setValue(stringAddressData);
    }

    public CommunicationStringAddressData getStringAddressData()
    {
        return stringAddressDataProperty.getValue();
    }

    public Property<CommunicationStringAddressData> communicationStringAddressDataProperty()
    {
        return stringAddressDataProperty;
    }

    public void addTaggable(Taggable taggable)
    {
        weakTaggableSet.add(new WeakReference<>(taggable));
    }

    public void removeTaggable(Taggable taggable)
    {
        weakTaggableSet.removeIf(weakRef ->
        {
            var lTaggable = weakRef.get();
            return lTaggable == null || lTaggable == taggable;
        });
    }

    public boolean isAnyTaggableActive()
    {
        var it = weakTaggableSet.iterator();
        while(it.hasNext())
        {
            var taggable = it.next().get();
            if(taggable == null)
            {
                it.remove();
                continue;
            }

            if(taggable.isActive())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void delete()
    {
        super.delete();
    }

}
