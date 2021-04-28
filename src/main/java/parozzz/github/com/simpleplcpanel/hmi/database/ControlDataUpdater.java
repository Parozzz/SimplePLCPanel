package parozzz.github.com.simpleplcpanel.hmi.database;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommThread;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ControlDataUpdater<C extends CommThread>
{
    protected final ControlContainerDatabase controlContainerDatabase;
    protected final C commThread;

    protected final Set<ControlWrapper<?>> newValueControlWrapperSet;
    private final Map<ControlWrapper<?>, Runnable> newValueRunnableMap;

    public ControlDataUpdater(ControlContainerDatabase controlContainerDatabase, C commThread)
    {
        this.controlContainerDatabase = controlContainerDatabase;
        this.commThread = commThread;

        this.newValueControlWrapperSet = new HashSet<>();
        this.newValueRunnableMap = new HashMap<>();
    }

    void bindControlWrapper(ControlWrapper<?> controlWrapper)
    {
        Runnable internalValueRunnable = () -> newValueControlWrapperSet.add(controlWrapper);
        controlWrapper.getValue().getInternalValue().addNewValueRunnable(internalValueRunnable);
        newValueRunnableMap.put(controlWrapper, internalValueRunnable);
    }

    void unbindControlWrapper(ControlWrapper<?> controlWrapper)
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
