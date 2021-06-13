package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

public abstract class CommunicationThread<T extends CommunicationConnectionParams>
        extends FXObject
        implements Loggable
{
    private final NetworkCommunicationManager<T> communicationManager;
    private final Thread thread;

    private volatile boolean oldActive;
    private volatile boolean active;
    private volatile boolean stop;

    protected volatile T communicationParams;
    private volatile boolean newConnectionParams = false;

    protected volatile boolean update;
    private volatile boolean alwaysRetryConnection;
    private volatile int timeBetweenRetries;

    private boolean firstConnection;
    private boolean connectedTried;

    public CommunicationThread(String threadName, NetworkCommunicationManager<T> communicationManager)
    {
        this.communicationManager = communicationManager;

        this.thread = new Thread(this::threadRun);
        thread.setName(threadName);

        active = false; //Start as inactive. It needs to be select to activate
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        communicationManager.alwaysRetryConnectionProperty().addListener((observable, oldValue, newValue) ->
                alwaysRetryConnection = newValue
        );

        communicationManager.timeBetweenRetriesProperty().addListener((observable, oldValue, newValue) ->
                timeBetweenRetries = newValue.intValue()
        );

        communicationManager.connectionParamsProperty().addListener((observable, oldValue, newValue) ->
        {
            communicationParams = newValue;
            newConnectionParams = communicationParams != null;
        });
    }

    @Override
    public void onSetupComplete()
    {
        super.onSetupComplete();

        this.thread.start();

        alwaysRetryConnection = communicationManager.isAlwaysRetryConnection();
        timeBetweenRetries = communicationManager.getTimeBetweenRetries();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        this.stop = true;
        try
        {
            this.thread.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized boolean isActive()
    {
        return active;
    }

    public synchronized void setActive(boolean active)
    {
        this.active = active;
    }

    private void threadRun()
    {
        while (true)
        {
            if (stop)
            {
                return;
            }

            if (!active)
            {
                if (oldActive && this.isConnected())
                {
                    this.disconnect();
                }

                oldActive = false;

                //This should stop the annoying wait for the sleep to finish stuff
                this.sleepWithChecks(10);
                continue;
            }

            oldActive = true;

            try
            {
                if (newConnectionParams)
                {
                    updateConnectionParams();
                    newConnectionParams = false;
                    connectedTried = false;
                }

                if (!this.isConnected())
                {
                    if (!alwaysRetryConnection && connectedTried)
                    {
                        Thread.sleep(1000);
                        continue;
                    }

                    connectedTried = true;
                    if (!connect())
                    {
                        this.sleepWithChecks(timeBetweenRetries);
                        continue;
                    }
                }

                if (update)
                {
                    this.update();
                    update = false;
                }

                Thread.sleep(50);
            } catch (Exception exception)
            {
                MainLogger.getInstance().error("Error while running CommThread", exception, this);
            }
        }
    }

    public synchronized boolean isUpdating()
    {
        return update;
    }

    public synchronized void doUpdate()
    {
        //Allow to update only if the PLC is connected!
        //Otherwise it will stay in update forever!
        if (this.isConnected())
        {
            update = true;
        }
    }

    public abstract void disconnect();

    public abstract boolean isConnected();

    public abstract boolean connect();

    public abstract void updateConnectionParams();

    public abstract void update();

    protected void sleepWithChecks(int seconds)
    {
        try
        {
            var oldActive = active;
            //This should stop the annoying wait for the sleep to finish stuff
            for (var x = 0; x < seconds * 2; x++)
            {
                Thread.sleep(500);
                //In case the thread come active from inactivity, break from here!
                if (oldActive != active || stop)
                {
                    break;
                }
            }
        } catch (InterruptedException interruptedException)
        {
            interruptedException.printStackTrace();
        }

    }

    @Override
    public String log()
    {
        return "CommunicationParams {" + (communicationParams == null ? "none" : communicationParams.log()) + "}";
    }

}
