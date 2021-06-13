package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TreeItem;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataArray;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.updater.ModbusTCPValueUpdater;
import parozzz.github.com.simpleplcpanel.hmi.tags.updater.SiemensPLCValueUpdater;
import parozzz.github.com.simpleplcpanel.hmi.tags.updater.TagValueUpdater;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.util.Cooldown;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class TagsManager extends FXController implements Iterable<Tag>, Loggable
{
    public enum ChangeType
    {
        ADD,
        REMOVE;
    }

    public static final LastInternalID LAST_INTERNAL_ID = new LastInternalID(); //0 is ALWAYS the root

    private final CommunicationDataHolder communicationDataHolder;

    private final Set<Tag> tagSet;
    private final Map<Integer, Tag> tagMap;
    private final List<BiConsumer<Tag, TagsManager.ChangeType>> tagMapChangeList;

    private final Map<CommunicationType<?>, TagValueUpdater<?>> tagValueUpdaterMap;
    private TagValueUpdater<?> activeTagValueUpdater;
    private boolean newCommunicationType;

    private final Cooldown nextUpdaterCooldown = new Cooldown(200);
    private boolean parseUpdatedData;

    private final Tag rootFolderTag;

    public TagsManager(CommunicationDataHolder communicationDataHolder)
    {
        this.communicationDataHolder = communicationDataHolder;

        this.tagSet = new HashSet<>();
        this.tagMap = new HashMap<>();
        this.tagMapChangeList = new ArrayList<>();

        (this.rootFolderTag = new FolderTag("root", 0)).init(this);

        this.tagValueUpdaterMap = new HashMap<>();
        this.addTagValueUpdater(
                SiemensPLCValueUpdater.createInstance(this, communicationDataHolder)
        ).addTagValueUpdater(
                ModbusTCPValueUpdater.createInstance(this, communicationDataHolder)
        );
    }

    private TagsManager addTagValueUpdater(TagValueUpdater<?> tagValueUpdater)
    {
        this.addFXChild(tagValueUpdater);
        tagValueUpdaterMap.put(tagValueUpdater.getCommunicationType(), tagValueUpdater);
        return this;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        super.serializableDataSet.addInt("LastInternalID", LAST_INTERNAL_ID.getProperty());

        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                newCommunicationType = true
        );
        communicationDataHolder.getCommunicationStage().addCommunicationTypeListener(communicationType ->
                tagSet.stream()
                        .filter(CommunicationTag.class::isInstance)
                        .map(CommunicationTag.class::cast)
                        .forEach(communicationTag -> communicationTag.updateCommunicationType(communicationType))
        );
    }

    @Override
    public void onLoop()
    {
        super.onLoop();

        if(newCommunicationType)
        {
            newCommunicationType = false;
            this.changeSelectedDataUpdater();
        }

        this.updateSelectedDataUpdater();
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();

        newCommunicationType = true;
    }

    public TreeItem<Tag> getRootItem()
    {
        return this.rootFolderTag.getTreeItem();
    }

    public void addTag(Tag tag)
    {
        this.addTag(null, tag);
    }

    public void addTag(Tag folderTag, Tag tag)
    {
        if (tag.getTreeItem() == null && tagSet.add(tag))
        {
            tagMap.put(tag.getInternalId(), tag);

            folderTag = folderTag == null ? rootFolderTag : folderTag;

            var folderTreeItem = folderTag.getTreeItem();
            if (folderTreeItem != null)
            {
                folderTreeItem.getChildren().add(tag.init(this));
            }

            if (tag instanceof CommunicationTag)
            {
                ((CommunicationTag) tag).updateCommunicationType(communicationDataHolder.getCurrentCommunicationType());
            }

            tag.addDeleteRunnable(() -> this.removeTag(tag));
            tagMapChangeList.forEach(consumer -> consumer.accept(tag, TagsManager.ChangeType.ADD));
        }
    }

    public void removeTag(Tag tag)
    {
        if (tagSet.remove(tag) && tagMap.remove(tag.getInternalId(), tag))
        {
            tagMapChangeList.forEach(consumer -> consumer.accept(tag, TagsManager.ChangeType.REMOVE));
        }
    }

    public Tag getTagFromId(int internalId)
    {
        return tagMap.get(internalId);
    }

    public void addTagMapChangeList(BiConsumer<Tag, TagsManager.ChangeType> consumer)
    {
        this.tagMapChangeList.add(consumer);
    }

    private void changeSelectedDataUpdater()
    {
        if(activeTagValueUpdater != null)
        {
            //If i have to change data updater, i need to do it only when the selected data updater has finished (aka is ready)
            if(activeTagValueUpdater.isActive())
            {
                return;
            }

            activeTagValueUpdater.setActive(false);
        }

        activeTagValueUpdater = tagValueUpdaterMap.get(communicationDataHolder.getCurrentCommunicationType());
        if (activeTagValueUpdater != null)
        {
            activeTagValueUpdater.setActive(true);
        }
    }

    private void updateSelectedDataUpdater()
    {
        if (activeTagValueUpdater == null)
        {
            return;
        }

        if (!activeTagValueUpdater.isReady())
        {
            //Keep the cooldown refreshed so when the updater is ready, will wait the full time
            nextUpdaterCooldown.createStamp();
            return;
        }

        //If the data updater is ready it means the plc thread has finished, so i update the read data
        if (parseUpdatedData)
        {
            activeTagValueUpdater.parseReadData();
            parseUpdatedData = false;
        }
        //And then wait some time for the next update
        if (nextUpdaterCooldown.passed())
        {
            activeTagValueUpdater.update();
            parseUpdatedData = true;
        }
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = super.serialize();

        var rootTreeItem = rootFolderTag.getTreeItem();
        if (rootTreeItem != null)
        {
            var rootJSONDataArray = new JSONDataArray();
            this.serializeTreeItem(rootTreeItem, rootJSONDataArray);
            jsonDataMap.set("Root", rootJSONDataArray);
        }

        return jsonDataMap;
    }

    @Nullable
    private void serializeTreeItem(TreeItem<Tag> treeItem,
            JSONDataArray folderJSONDataArray)
    {
        if (treeItem == null)
        {
            return;
        }

        for (var child : treeItem.getChildren())
        {
            var childTag = child.getValue();
            if (childTag == null)
            {
                continue;
            }

            var childJSONDataMap = new JSONDataMap();
            childJSONDataMap.set("InternalID", childTag.getInternalId());
            childJSONDataMap.set("Key", childTag.getKey());

            if (childTag instanceof FolderTag)
            {
                childJSONDataMap.set("Folder", true);

                var childFolderJSONDataArray = new JSONDataArray();
                this.serializeTreeItem(childTag.getTreeItem(), childFolderJSONDataArray);
                childJSONDataMap.set("SubKeys", childFolderJSONDataArray);
            } else if (childTag instanceof CommunicationTag)
            {
                var commTag = (CommunicationTag) childTag;

                childJSONDataMap.set("Local", commTag.isLocal());

                var stringAddressDataJSONDataMap = new JSONDataMap();
                for (var communicationType : CommunicationType.values())
                {
                    var stringAddressData = commTag.getCommunicationTypeStringAddressData(communicationType);
                    if (stringAddressData != null)
                    {
                        stringAddressDataJSONDataMap.set(communicationType.getName(), stringAddressData.getStringData());
                    }
                }
                childJSONDataMap.set("CommData", stringAddressDataJSONDataMap);
            }

            folderJSONDataArray.add(childJSONDataMap);
        }
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);

        if (jsonDataMap == null || jsonDataMap.size() == 0)
        {
            return;
        }

        var rootTreeItem = rootFolderTag.getTreeItem();
        var rootJSONDataArray = jsonDataMap.getArray("Root");
        if (rootTreeItem == null || rootJSONDataArray == null)
        {
            return;
        }

        for (var obj : rootJSONDataArray)
        {
            if (!(obj instanceof JSONObject))
            {
                continue;
            }

            var childJSONDataMap = new JSONDataMap((JSONObject) obj);
            this.parseTag(rootFolderTag, childJSONDataMap);
        }
    }

    private void parseTag(Tag folderTag, JSONDataMap jsonDataMap)
    {
        var internalID = jsonDataMap.getNumber("InternalID");
        var key = jsonDataMap.getString("Key");

        if (internalID == null || key == null)
        {
            return;
        }

        if (jsonDataMap.getBoolean("Folder"))
        {
            var childFolderTag = new FolderTag(key, internalID.intValue());
            this.addTag(folderTag, childFolderTag);

            var folderTreeItem = childFolderTag.getTreeItem();
            if (folderTreeItem == null)
            {
                return;
            }

            var subKeysJSONDataArray = jsonDataMap.getArray("SubKeys");
            if (subKeysJSONDataArray != null)
            {
                for (var obj : subKeysJSONDataArray)
                {
                    if (!(obj instanceof JSONObject))
                    {
                        continue;
                    }

                    var childJSONDataMap = new JSONDataMap((JSONObject) obj);
                    this.parseTag(childFolderTag, childJSONDataMap);
                }
            }
        } else
        {
            var tag = new CommunicationTag(key, internalID.intValue());
            tag.setLocal(jsonDataMap.getBoolean("Local"));

            var commDataJSONDataMap = jsonDataMap.getMap("CommData");
            if (commDataJSONDataMap != null)
            {
                for (var communicationType : CommunicationType.values())
                {
                    var stringData = commDataJSONDataMap.getString(communicationType.getName());
                    if (stringData != null && !stringData.isEmpty())
                    {
                        var addressStringData = communicationType.parseStringAddressData(stringData);
                        if (addressStringData != null)
                        {
                            tag.setCommunicationTypeStringAddressData(communicationType, addressStringData);
                        }
                    }
                }
            }

            //This needs to be done after because it bounds values together and then
            //WON'T be able to set the whole "CommData" above!
            this.addTag(folderTag, tag);
        }
    }

    public Stream<Tag> stream()
    {
        return tagSet.stream();
    }

    @Override
    public Iterator<Tag> iterator()
    {
        return Collections.unmodifiableSet(tagSet).iterator();
    }

    @Override
    public String log()
    {
        return "ActiveControlDataUpdater: " + this.getSelectedControlDataUpdaterName();
    }

    private String getSelectedControlDataUpdaterName()
    {
        return activeTagValueUpdater == null
                ? "none"
                : activeTagValueUpdater.getClass().getSimpleName();
    }

    public static class LastInternalID
    {
        private final IntegerProperty property;

        public LastInternalID()
        {
            this.property = new SimpleIntegerProperty(1);
        }

        public IntegerProperty getProperty()
        {
            return property;
        }

        public int getAndAdd()
        {
            var value = property.get();
            property.set(value + 1);
            return value;
        }
    }
}
