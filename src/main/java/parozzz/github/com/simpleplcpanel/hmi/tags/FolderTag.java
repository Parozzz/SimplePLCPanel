package parozzz.github.com.simpleplcpanel.hmi.tags;

import parozzz.github.com.simpleplcpanel.hmi.util.ContextMenuBuilder;

public class FolderTag extends Tag
{
    public FolderTag(String key)
    {
        super(key);

        //ContextMenuBuilder.builder(this.getContextMenu())
    }

    @Override
    public void delete()
    {
        super.delete();
    }
}
