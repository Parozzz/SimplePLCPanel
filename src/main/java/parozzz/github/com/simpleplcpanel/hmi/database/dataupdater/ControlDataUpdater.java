package parozzz.github.com.simpleplcpanel.hmi.database.dataupdater;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.database.ControlContainerDatabase;

import java.util.*;

public abstract class ControlDataUpdater<C extends CommThread<?>>
{
    protected final CommunicationType communicationType;
    protected final ControlContainerDatabase controlContainerDatabase;
    protected final CommunicationDataHolder communicationDataHolder;
    protected final C commThread;

    protected final Set<ControlWrapper<?>> newValueControlWrapperSet;
    private final Map<ControlWrapper<?>, Runnable> newValueRunnableMap;

    public ControlDataUpdater(CommunicationType communicationType,
            ControlContainerDatabase controlContainerDatabase,
            CommunicationDataHolder communicationDataHolder, C commThread)
    {
        this.communicationType = communicationType;
        this.controlContainerDatabase = controlContainerDatabase;
        this.communicationDataHolder = communicationDataHolder;
        this.commThread = commThread;

        this.newValueControlWrapperSet = new HashSet<>();
        this.newValueRunnableMap = new HashMap<>();
    }

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
    }

    public boolean isReady()
    {
        return !commThread.isUpdating();
    }

    //This is to allow the data to be set in the JavaFX Thread
    public abstract void parseReadData();

    public abstract void update();
}
