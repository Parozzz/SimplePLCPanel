package parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;

public abstract class CellFactoryHandler<T>
{
    protected final TreeTableCell<Tag, T> cell;
    protected final ChangeListener<TreeItem<Tag>> changeTreeItemListener;

    public CellFactoryHandler()
    {
        this.cell = new TreeTableCell<>();
        this.changeTreeItemListener = this::updateTreeItem;
    }

    public void init()
    {
        cell.setPadding(new Insets(-1, 2, -1, 2));
        cell.tableRowProperty().addListener((rowObservable, oldRow, newRow) ->
        {
            if(oldRow != null)
            {
                oldRow.treeItemProperty().removeListener(changeTreeItemListener);
            }

            if(newRow != null)
            {
                newRow.treeItemProperty().addListener(changeTreeItemListener);
            }
        });
    }

    public TreeTableCell<Tag, T> getCell()
    {
        return cell;
    }

    protected abstract void registerTag(CommunicationTag tag);

    protected abstract void unregisterTag(CommunicationTag tag);

    protected abstract void setGraphic();

    private void updateTreeItem(ObservableValue<? extends TreeItem<Tag>> observable,
            TreeItem<Tag> oldValue, TreeItem<Tag> newValue)
    {
        if(oldValue != null)
        {
            var tag = oldValue.getValue();
            if(tag instanceof CommunicationTag)
            {
                this.unregisterTag((CommunicationTag) tag);
            }
        }

        Tag tag;
        if(newValue == null || !((tag = newValue.getValue()) instanceof CommunicationTag))
        {
            cell.setGraphic(null);
            return;
        }

        this.registerTag((CommunicationTag) tag);
        this.setGraphic();
    }
}
