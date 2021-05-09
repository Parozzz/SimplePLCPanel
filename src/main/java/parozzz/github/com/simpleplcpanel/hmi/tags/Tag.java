package parozzz.github.com.simpleplcpanel.hmi.tags;

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

public class Tag
{
    private final ObservableValue<String> keyValue;
    private final Set<Runnable> deleteRunnableSet;
    private final ContextMenu contextMenu;

    private final Property<CommunicationType<?>> communicationTypeProperty;
    private final Property<CommunicationStringAddressData> stringAddressDataProperty;

    private TagSplitPane splitPane;

    public Tag(String key, CommunicationType<?> communicationType)
    {
        this.keyValue = new ReadOnlyObjectWrapper<>(key);
        this.deleteRunnableSet = new HashSet<>();

        this.communicationTypeProperty = new SimpleObjectProperty<>(communicationType);
        this.stringAddressDataProperty = new SimpleObjectProperty<>();

        this.contextMenu = ContextMenuBuilder.builder().simple("Delete", this::delete)
                .getContextMenu();
    }

    public void setSplitPane(TagSplitPane splitPane)
    {
        Validate.needTrue("Trying to set the TreeItem for a tag twice", this.splitPane == null);
        this.splitPane = splitPane;
    }

    @Nullable
    public TagSplitPane getSplitPane()
    {
        return splitPane;
    }

    public String getKey()
    {
        return keyValue.getValue();
    }

    public ObservableValue<String> keyValueProperty()
    {
        return keyValue;
    }

    public CommunicationType<?> getCommunicationType()
    {
        return communicationTypeProperty.getValue();
    }

    public Property<CommunicationType<?>> communicationTypeProperty()
    {
        return communicationTypeProperty;
    }

    public CommunicationStringAddressData getStringAddressData()
    {
        return stringAddressDataProperty.getValue();
    }

    public Property<CommunicationStringAddressData> communicationStringAddressDataProperty()
    {
        return stringAddressDataProperty;
    }

    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }

    private void delete()
    {
        deleteRunnableSet.forEach(Runnable::run);
    }

    public void addDeleteRunnable(Runnable runnable)
    {
        deleteRunnableSet.add(runnable);
    }
}
