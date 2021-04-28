package parozzz.github.com.simpleplcpanel.util;

import parozzz.github.com.simpleplcpanel.logger.MainLogger;

public abstract class LoopThread extends Thread
{
    private volatile boolean stop = false;

    public abstract void setup() throws Exception;

    public abstract void loop() throws Exception;

    public synchronized void setStop()
    {
        this.stop = true;
    }

    @Override
    public final void run()
    {
        try
        {
            setup();
        }
        catch (Exception exception)
        {
            MainLogger.getInstance().warning("Error in Setup", exception, this);
        }


        while (true)
        {
            if(stop)
            {
                return;
            }

            try
            {
                loop();
            } catch (Exception exception)
            {
                MainLogger.getInstance().warning("Error in Loop", exception, this);
            }
        }
    }
}
