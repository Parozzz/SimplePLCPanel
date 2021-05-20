package parozzz.github.com.simpleplcpanel.hmi.database.dataupdater;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.database.ControlContainerDatabase;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;
import parozzz.github.com.simpleplcpanel.hmi.tags.stage.TagStage;

import java.util.*;

public abstract class ControlDataUpdater<C extends CommThread<?>> extends FXObject
{
    protected final TagStage tagStage;
    protected final CommunicationType<?> communicationType;
    protected final ControlContainerDatabase controlContainerDatabase;
    protected final CommunicationDataHolder communicationDataHolder;
    protected final C commThread;

    //This need to be a set to avoid duplicate Tag!
    protected final Set<Tag> needWriteTagSet;
    protected final BooleanProperty activeProperty;

    public ControlDataUpdater(TagStage tagStage,
            CommunicationType<?> communicationType,
            ControlContainerDatabase controlContainerDatabase,
            CommunicationDataHolder communicationDataHolder, C commThread)
    {
        this.tagStage = tagStage;
        this.communicationType = communicationType;
        this.controlContainerDatabase = controlContainerDatabase;
        this.communicationDataHolder = communicationDataHolder;
        this.commThread = commThread;

        this.needWriteTagSet = new HashSet<>();

        this.activeProperty = new SimpleBooleanProperty(false);
    }

    @Override
    public void setup()
    {
        super.setup();

        activeProperty.addListener((observable, oldValue, newValue) ->
        {
            if(newValue == null || !newValue)
            {
                needWriteTagSet.clear();
            }
        });

        tagStage.addTagMapChangeList((tag, changeType) ->
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
        return commThread.isConnected() && !commThread.isUpdating();
    }

    //This is to allow the data to be set in the JavaFX Thread
    public abstract void parseReadData();

    public abstract void update();
}
