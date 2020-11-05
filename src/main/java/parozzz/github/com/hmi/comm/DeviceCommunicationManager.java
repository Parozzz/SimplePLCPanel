package parozzz.github.com.hmi.comm;

import javafx.scene.Parent;
import parozzz.github.com.hmi.FXObject;

import java.io.IOException;

public abstract class DeviceCommunicationManager<T extends CommThread> extends FXObject
{
    protected final T commThread;
    protected boolean active;

    public DeviceCommunicationManager(String name, T commThread) throws IOException
    {
        super(name);

        this.commThread = commThread;
    }

    public T getCommThread()
    {
        return commThread;
    }

    public abstract Parent getParent();

    public void setActive(boolean active)
    {
        commThread.setActive(active);
    }

    public boolean isActive()
    {
        return commThread.isActive();
    }
}
