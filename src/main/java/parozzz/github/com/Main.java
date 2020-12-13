package parozzz.github.com;

import javafx.application.Application;
import parozzz.github.com.hmi.JavaFXMain;
import parozzz.github.com.logger.MainLogger;
import parozzz.github.com.util.Util;

import java.net.UnknownHostException;

public class Main
{
    public static StartProperties START_PROPERTIES;

    public static void main(String[] args) throws UnknownHostException
    {
        START_PROPERTIES = new StartProperties();
        START_PROPERTIES.setup();
        if (START_PROPERTIES.getBoolean(StartProperties.PropertyEnum.SHOW_EMBEDDED_KEYBOARD))
        {
            //It needs to be done here before launching JavaFX applet otherwise it won't work
            System.setProperty("com.sun.javafx.touch", "true");
            System.setProperty("com.sun.javafx.isEmbedded", "true");
            System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");
        }

        if (START_PROPERTIES.getBoolean(StartProperties.PropertyEnum.MODBUS_DEBUG))
        {
            System.setProperty("net.wimpi.modbus.debug", "true");
        }

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
