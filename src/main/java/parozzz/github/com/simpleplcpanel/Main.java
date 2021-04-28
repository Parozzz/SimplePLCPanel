package parozzz.github.com.simpleplcpanel;

import javafx.application.Application;
import parozzz.github.com.simpleplcpanel.hmi.JavaFXMain;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.Util;

import java.net.UnknownHostException;

public class Main
{
    public static StartProperties START_PROPERTIES;

    public static void main(String[] args) throws UnknownHostException
    {
        START_PROPERTIES = new StartProperties();
        START_PROPERTIES.setup();
        START_PROPERTIES.applyGeneralProperties();

        MainLogger.getInstance().start();

        try
        {
            if(args.length != 0)
            {
                var toWait = Util.parseInt(args[0], 0);
                Thread.sleep(toWait);
            }

            new Main().start();
        } catch (InterruptedException e)
        {
            MainLogger.getInstance().warning("", e, null);
        }
    }

    public void start() throws InterruptedException
    {
        Application.launch(JavaFXMain.class);
    }
}
