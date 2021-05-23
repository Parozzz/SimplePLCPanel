package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

public abstract class CommThread<T extends CommunicationConnectionParams>
        extends Thread
        implements Loggable
{
    private volatile boolean oldActive;
    private volatile boolean active;
    private volatile boolean stop;

    protected volatile T communicationParams;
    private volatile boolean newConnectionParams = false;

    protected volatile boolean update;
    private volatile boolean alwaysRetryConnection;
    private boolean connectedTried;
    private volatile int timeBetweenRetries;

    public CommThread()
    {
        active = false; //Start as inactive. It needs to be select to activate
    }

    public synchronized void setStop()
    {
        this.stop = true;
    }

    public synchronized boolean isActive()
    {
        return active;
    }

    public synchronized void setActive(boolean active)
    {
        this.active = active;
    }

    public synchronized void setAlwaysRetryConnection(boolean alwaysRetryConnection)
    {
        this.alwaysRetryConnection = alwaysRetryConnection;
    }

    public synchronized void setConnectionParameters(T communicationParams)
    {
        this.communicationParams = communicationParams;
        newConnectionParams = true;
    }

    public synchronized void setTimeBetweenRetries(int timeBetweenRetries)
    {
        this.timeBetweenRetries = timeBetweenRetries;
    }

    @Override
    public final void run()
    {
        while(true)
        {
            if(stop)
            {
                return;
            }

            if(!active)
            {
                if(oldActive && this.isConnected())
                {
                    this.disconnect();
                }

                oldActive = false;

                //This should stop the annoying wait for the sleep to finish stuff
                this.sleepWithStopCheck(10);
                continue;
            }

            oldActive = true;

            try
            {
                this.loop();
            }
            catch(Exception exception)
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
        if(this.isConnected())
        {
            update = true;
        }
    }

    public abstract void disconnect();

    public abstract boolean isConnected();

    public abstract boolean connect();

    public abstract void updateConnectionParams();

    public final void loop() throws Exception
    {
        if(newConnectionParams)
        {
            updateConnectionParams();
            newConnectionParams = false;
            connectedTried = false;
        }

        if(!this.isConnected())
        {
            if(!alwaysRetryConnection && connectedTried)
            {
                Thread.sleep(1000);
                return;
            }

            connectedTried = true;
            if(!connect())
            {
                this.sleepWithStopCheck(timeBetweenRetries);
                return;
            }
        }

        if(update)
        {
            this.update();
            update = false;
        }

        Thread.sleep(50);
    }

    public abstract void update();

    protected void sleepWithStopCheck(int seconds)
    {
        try
        {
            //This should stop the annoying wait for the sleep to finish stuff
            for(var x = 0; x < seconds; x++)
            {
                Thread.sleep(1000);
                //In case the thread come active from inactivity, break from here!
                if(!active || stop)
                {
                    break;
                }
            }
        }
        catch(InterruptedException interruptedException)
        {
            interruptedException.printStackTrace();
        }

    }

    @Override
    public String log()
    {
        return "CommunicationParams {" + communicationParams.log() + "}";
    }

}
