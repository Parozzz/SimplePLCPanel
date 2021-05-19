package parozzz.github.com.simpleplcpanel.hmi.database.dataupdater;

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
    private final static String WRITE_INTERMEDIATE_KEY = "ControlDataUpdater";

    protected final TagStage tagStage;
    protected final CommunicationType<?> communicationType;
    protected final ControlContainerDatabase controlContainerDatabase;
    protected final CommunicationDataHolder communicationDataHolder;
    protected final C commThread;

    protected final Set<Tag> needWriteTagSet;

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
    }

    @Override
    public void setup()
    {
        super.setup();

        tagStage.addTagMapChangeList((tag, changeType) ->
        {
            switch (changeType)
            {
                case ADD:
                    tag.getWriteIntermediate().addNewValueRunnable(
                            WRITE_INTERMEDIATE_KEY,
                            () -> needWriteTagSet.add(tag)
                    );
                    break;
                case REMOVE:
                    tag.getWriteIntermediate().removeNewValueRunnable(WRITE_INTERMEDIATE_KEY);
                    break;
            }
        });
    }
/*
    public void bindControlWrapper(ControlWrapper<?> controlWrapper)
    {
        Runnable internalValueRunnable = () -> newValueControlWrapperSet.add(controlWrapper);
        controlWrapper.getValue().getInternalValue().addNewValueRunnable(internalValueRunnable);
        newValueRunnableMap.put(controlWrapper, internalValueRunnable);
    }

    public void unbindControlWrapper(ControlWrapper<?> controlWrapper)
    {
        var runnable = newValueRunnableMap.remove(controlWrapper);
        if(runnable != null)
        {
            controlWrapper.getValue().getInternalValue().removeNewValueRunnable(runnable);
        }
    }*/

    public boolean isReady()
    {
        return !commThread.isUpdating();
    }

    //This is to allow the data to be set in the JavaFX Thread
    public abstract void parseReadData();

    public abstract void update();
}
