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
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;
import parozzz.github.com.simpleplcpanel.util.Validate;

import java.util.HashSet;
import java.util.Set;

public abstract class Tag
{
    private final ObservableValue<String> keyValue;
    private final Set<Runnable> deleteRunnableSet;
    private final ContextMenu contextMenu;

    private TreeItem<Tag> treeItem;

    public Tag(String key)
    {
        this.keyValue = new ReadOnlyObjectWrapper<>(key);
        this.deleteRunnableSet = new HashSet<>();

        this.contextMenu = ContextMenuBuilder.builder().simple("Delete", this::delete)
                .getContextMenu();
    }

    public TreeItem<Tag> createTreeItem()
    {
        Validate.needTrue("Trying to set a TreeItem to a tag twice", this.treeItem == null);
        return this.treeItem = new TreeItem<>();
        //return treeItem;
    }

    public void setTreeItem(TreeItem<Tag> treeItem)
    {
        Validate.needTrue("Trying to set a TreeItem to a tag twice", this.treeItem == null);
        this.treeItem = treeItem;
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

    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }

    public void delete()
    {
        deleteRunnableSet.forEach(Runnable::run);
    }

    public void addDeleteRunnable(Runnable runnable)
    {
        deleteRunnableSet.add(runnable);
    }
}
