package parozzz.github.com.simpleplcpanel.hmi.tags;

import com.sun.source.tree.Tree;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.MixedIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.HashSet;
import java.util.Set;

public abstract class Tag
{
    private final int internalId;
    private final ObservableValue<String> keyValue;
    private final Set<Runnable> deleteRunnableSet;

    protected TagsManager tagsManager;
    protected TreeItem<Tag> treeItem;

    private final MixedIntermediate readIntermediate;
    private final MixedIntermediate writeIntermediate;

    public Tag(String key)
    {
        this(key, TagsManager.LAST_INTERNAL_ID.getAndAdd());
    }

    public Tag(String key, int internalId)
    {
        this.internalId = internalId;
        this.keyValue = new ReadOnlyObjectWrapper<>(key);
        this.deleteRunnableSet = new HashSet<>();

        this.readIntermediate = new MixedIntermediate();
        this.writeIntermediate = new MixedIntermediate();
    }

    public int getInternalId()
    {
        return internalId;
    }

    public String getKey()
    {
        return keyValue.getValue();
    }

    public ObservableValue<String> keyValueProperty()
    {
        return keyValue;
    }

    public ValueIntermediate getReadIntermediate()
    {
        return readIntermediate;
    }

    public ValueIntermediate getWriteIntermediate()
    {
        return writeIntermediate;
    }

    @Nullable
    public TreeItem<Tag> getTreeItem()
    {
        return treeItem;
    }

    public TreeItem<Tag> init(TagsManager tagsManager)
    {
        Validate.needTrue("Trying to initialize a Tag twice",
                this.treeItem == null && this.tagsManager == null);

        this.tagsManager = tagsManager;
        this.treeItem = new TreeItem<>(this);
        return treeItem;
    }

    @Nullable
    public ContextMenu createContextMenu()
    {
        return ContextMenuBuilder.builder()
                .simple("Delete", this::delete)
                .simple("Test", () -> System.out.println(this.getHierarchicalKey()))
                .getContextMenu();
    }

    public String getHierarchicalKey()
    {
        if(this.treeItem == null)
        {
            return "";
        }

        var key = this.getKey();

        var treeItemParent = this.treeItem.getParent();
        while(treeItemParent != null && treeItemParent != tagsManager.getRootItem())
        {
            var tagParent = treeItemParent.getValue();
            if(tagParent == null)
            {
                return "";
            }

            key = tagParent.getKey() + "." + key;
            treeItemParent = treeItemParent.getParent();
        }

        return key;
    }

    public void delete()
    {
        if(tagsManager == null || treeItem == null)
        {
            return;
        }

        var parentTreeItem = treeItem.getParent();
        if(parentTreeItem != null)
        {
            parentTreeItem.getChildren().remove(treeItem);
        }

        deleteRunnableSet.forEach(Runnable::run);
    }

    public void addDeleteRunnable(Runnable runnable)
    {
        deleteRunnableSet.add(runnable);
    }

    public int hashCode()
    {
        return internalId;
    }

    public boolean equals(Object obj)
    {
        return this == obj || (obj instanceof Tag && this.internalId == ((Tag) obj).internalId);
    }
}
