package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class CommThread<T extends CommunicationConnectionParams> extends Thread
{
    private volatile boolean oldActive;
    private volatile boolean active;
    private volatile boolean stop;

    protected volatile T communicationParams;
    protected volatile boolean newConnectionParams = false;
    private volatile boolean firstConnectionParamsReceived = false;

    protected volatile boolean update;

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

    public synchronized void setConnectionParameters(T communicationParams)
    {
        this.communicationParams = communicationParams;

        newConnectionParams = true;
        firstConnectionParamsReceived = true;
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
            } catch (Exception exception) {
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
        update = true;
    }

    public abstract void disconnect();

    public abstract boolean isConnected();

    public abstract boolean connect();

    public final void loop() throws Exception
    {
        while (!firstConnectionParamsReceived)
        {
            Thread.sleep(250);
        }

        if (!connect())
        {
            this.sleepWithStopCheck(10);
            return;
        }

        if (!update)
        {
            Thread.sleep(50);
            return;
        }

        this.update();
        update = false;
    }

    public abstract void update();

    protected void sleepWithStopCheck(int seconds)
    {
        var wasActive = this.active;
        try
        {
            //This should stop the annoying wait for the sleep to finish stuff
            for(var x = 0; x < seconds; x++)
            {
                Thread.sleep(1000);
                //In case the thread come active from inactivity, break from here!
                if(!wasActive && active)
                {
                    break;
                }

                if(stop)
                {
                    return;
                }
            }
        } catch (InterruptedException interruptedException)
        {
            interruptedException.printStackTrace();
        }

    }

}
