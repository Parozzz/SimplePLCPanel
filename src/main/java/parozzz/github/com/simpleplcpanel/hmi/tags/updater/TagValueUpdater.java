package parozzz.github.com.simpleplcpanel.hmi.tags.updater;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;

import java.util.HashSet;
import java.util.Set;

public abstract class TagValueUpdater<C extends CommunicationThread<?>> extends FXObject
{
    protected final TagsManager tagsManager;
    protected final CommunicationType<?> communicationType;
    protected final CommunicationDataHolder communicationDataHolder;
    protected final C communicationThread;

    //This need to be a set to avoid duplicate Tag!
    protected final Set<Tag> needWriteTagSet;
    protected final BooleanProperty activeProperty;

    public TagValueUpdater(TagsManager tagsManager,
            CommunicationType<?> communicationType,
            CommunicationDataHolder communicationDataHolder, C communicationThread)
    {
        this.tagsManager = tagsManager;
        this.communicationType = communicationType;
        this.communicationDataHolder = communicationDataHolder;
        this.communicationThread = communicationThread;

        this.needWriteTagSet = new HashSet<>();

        this.activeProperty = new SimpleBooleanProperty(false);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        activeProperty.addListener((observable, oldValue, newValue) ->
        {
            communicationThread.setActive(newValue);
            if(!newValue)
            {
                needWriteTagSet.clear();
            }
        });

        tagsManager.addTagMapChangeList((tag, changeType) ->
        {
            switch(changeType)
            {
                case ADD:
                    tag.getWriteIntermediate().addNewValueRunnable(
                            this,
                            () -> {
                                //Add Tag only if active!
                                if(this.isActive())
                                {
                                    needWriteTagSet.add(tag);
                                }
                            }
                    );
                    break;
                case REMOVE:
                    tag.getWriteIntermediate().removeNewValueRunnable(this);
                    break;
            }
        });
    }

    public CommunicationType<?> getCommunicationType()
    {
        return communicationType;
    }

    public boolean isActive()
    {
        return activeProperty.get();
    }

    public void setActive(boolean active)
    {
        activeProperty.set(active);
    }

    public BooleanProperty activeProperty()
    {
        return activeProperty;
    }

    public boolean isReady()
    {
        return communicationThread.isConnected() && !communicationThread.isUpdating();
    }

    //This is to allow the data to be set in the JavaFX Thread
    public abstract void parseReadData();

    public abstract void update();
}
