package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.*;
import javafx.scene.control.ContextMenu;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.MixedIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

import java.util.*;

public final class CommunicationTag extends Tag
{
    public enum TagProperty
    {
        NEED_READ,
        ACTIVE;
    }

    private final BooleanProperty localProperty;
    private final Property<CommunicationStringAddressData> stringAddressDataProperty;
    private Property<CommunicationStringAddressData> selectedStringAddressDataProperty;

    private final Map<CommunicationType<?>, Property<CommunicationStringAddressData>> stringAddressDataMap;
    private final Set<Taggable> taggableSet;

    private final MixedIntermediate readIntermediate;
    private final MixedIntermediate writeIntermediate;

    public CommunicationTag(String key, int internalID)
    {
        super(key, internalID);

        this.localProperty = new SimpleBooleanProperty(false);
        this.stringAddressDataProperty = new SimpleObjectProperty<>();

        this.stringAddressDataMap = new HashMap<>();
        this.taggableSet = new HashSet<>();

        this.readIntermediate = new MixedIntermediate();
        this.writeIntermediate = new MixedIntermediate();

        for(var communicationType : CommunicationType.values())
        {
            var stringAddressData = communicationType.supplyDefaultStringAddressData();
            if(stringAddressData != null)
            {
                var property = new SimpleObjectProperty<>(stringAddressData);
                stringAddressDataMap.put(communicationType, property);
            }
        }

        localProperty.addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null || newValue)
            {
                this.updateSelectedStringAddressDataProperty(selectedStringAddressDataProperty, null);
                return;
            }

            this.updateSelectedStringAddressDataProperty(null, selectedStringAddressDataProperty);
        });
    }

    public CommunicationTag(String key)
    {
        this(key, TagsManager.LAST_INTERNAL_ID.getAndAdd());
    }

    public ValueIntermediate getReadIntermediate()
    {
        return readIntermediate;
    }

    public ValueIntermediate getWriteIntermediate()
    {
        return writeIntermediate;
    }

    public boolean isLocal()
    {
        return localProperty.get();
    }

    public void setLocal(boolean local)
    {
        localProperty.set(local);
    }

    public BooleanProperty localProperty()
    {
        return localProperty;
    }

    public CommunicationStringAddressData getStringAddressData()
    {
        return stringAddressDataProperty.getValue();
    }

    public void setStringAddressData(CommunicationStringAddressData stringAddressData)
    {
        stringAddressDataProperty.setValue(stringAddressData);
    }

    public Property<CommunicationStringAddressData> communicationStringAddressDataProperty()
    {
        return stringAddressDataProperty;
    }

    @Nullable
    public ContextMenu createContextMenu()
    {
        return ContextMenuBuilder.builder(super.createContextMenu())
                .simple("Clone", this::addClone)
                .getContextMenu();
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
        if(oldValue != null)
        {
            oldValue.unbind();
        }

        if(localProperty.get())
        {
            stringAddressDataProperty.setValue(null);
            return;
        }

        if(newValue != null)
        {
            stringAddressDataProperty.setValue(newValue.getValue());
            newValue.bind(stringAddressDataProperty);
        }
    }

    @Nullable
    public CommunicationStringAddressData getCommunicationTypeStringAddressData(CommunicationType<?> communicationType)
    {
        var property = stringAddressDataMap.get(communicationType);
        return property == null ? null : property.getValue();
    }

    @Nullable
    public void setCommunicationTypeStringAddressData(CommunicationType<?> communicationType, CommunicationStringAddressData stringAddressData)
    {
        var property = stringAddressDataMap.get(communicationType);
        if(property != null)
        {
            property.setValue(stringAddressData);
        }
    }

    public void addTaggable(Taggable taggable)
    {
        taggableSet.add(taggable);
    }

    public void removeTaggable(Taggable taggable)
    {
        taggableSet.removeIf(lTaggable ->
                lTaggable == taggable
        );
    }

    public boolean hasProperty(TagProperty property)
    {
        switch (property)
        {
            case NEED_READ:
                for(var taggable : taggableSet)
                {
                    if(taggable.requireReading())
                    {
                        return true;
                    }
                }
                break;
            case ACTIVE:
                for(var taggable : taggableSet)
                {
                    if(taggable.isActive())
                    {
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public void delete()
    {
        super.delete();
    }

    @Override
    public CommunicationTag clone()
    {
        var clone = new CommunicationTag(super.getKey());

        clone.localProperty.set(localProperty.getValue());
        stringAddressDataMap.forEach((communicationType, stringAddressDataProperty) ->
        {
            var addressDataProperty = new SimpleObjectProperty<CommunicationStringAddressData>();
            addressDataProperty.setValue(stringAddressDataProperty.getValue());
            clone.stringAddressDataMap.put(communicationType, addressDataProperty);
        });

        return clone;
    }

    private void addClone()
    {
        if(super.tagsManager != null && treeItem != null)
        {
            var clone = this.clone();

            var parent = treeItem.getParent();
            if(parent != null)
            {
                tagsManager.addTag(parent.getValue(), clone);
            }
        }
    }
}
