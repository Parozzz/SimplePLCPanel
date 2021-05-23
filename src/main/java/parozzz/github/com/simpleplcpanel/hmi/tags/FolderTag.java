package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.scene.control.ContextMenu;
import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;

public final class FolderTag extends Tag
{
    public FolderTag(String key, int internalID)
    {
        super(key, internalID);

        //ContextMenuBuilder.builder(this.getContextMenu())
    }

    public FolderTag(String key)
    {
        super(key);

        //ContextMenuBuilder.builder(this.getContextMenu())
    }

    @Nullable
    public ContextMenu createContextMenu()
    {
        return ContextMenuBuilder.builder(super.createContextMenu())
                .simple("Add tag", this::addTag)
                .simple("Add folder", this::addFolder)
                .getContextMenu();
    }

    @Override
    public void delete()
    {
        super.delete();

        if(super.treeItem != null)
        {
            for(var child : treeItem.getChildren())
            {
                var childTag = child.getValue();
                if(childTag != null)
                {
                    childTag.delete();
                }
            }
        }
    }

    private void addTag()
    {
        if(super.tagsManager != null)
        {
            super.tagsManager.addTag(this, new CommunicationTag("NewTag"));
        }
    }

    private void addFolder()
    {
        if(super.tagsManager != null)
        {
            super.tagsManager.addTag(this, new FolderTag("NewFolder"));
        }
    }
}
