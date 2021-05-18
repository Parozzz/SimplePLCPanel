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
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.HashSet;
import java.util.Set;

public abstract class Tag
{
    private final ObservableValue<String> keyValue;
    private final Set<Runnable> deleteRunnableSet;

    protected TagStage tagStage;
    protected TreeItem<Tag> treeItem;

    public Tag(String key)
    {
        this.keyValue = new ReadOnlyObjectWrapper<>(key);
        this.deleteRunnableSet = new HashSet<>();
    }

    @Nullable
    public ContextMenu createContextMenu()
    {
        return ContextMenuBuilder.builder()
                .simple("Delete", this::delete)
                .getContextMenu();
    }

    public TreeItem<Tag> init(TagStage tagStage)
    {
        Validate.needTrue("Trying to initialize a Tag twice",
                this.treeItem == null && this.tagStage == null);

        this.tagStage = tagStage;
        this.treeItem = new TreeItem<>(this);
        return treeItem;
    }

    @Nullable
    public TreeItem<Tag> getTreeItem()
    {
        return treeItem;
    }

    public String getKey()
    {
        return keyValue.getValue();
    }

    public ObservableValue<String> keyValueProperty()
    {
        return keyValue;
    }

    public void delete()
    {
        if(tagStage == null || treeItem == null)
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
}
