package parozzz.github.com.hmi.comm;

import parozzz.github.com.logger.MainLogger;

public abstract class CommThread extends Thread
{
    private volatile boolean oldActive;
    private volatile boolean active;
    private volatile boolean stop;

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
                try
                {
                    Thread.sleep(10000);
                } catch (InterruptedException interruptedException)
                {
                    interruptedException.printStackTrace();
                }

                if(oldActive && this.isConnected())
                {
                    this.disconnect();
                }

                oldActive = false;
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

    public abstract void loop() throws Exception;

}
