package parozzz.github.com.simpleplcpanel.hmi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class JavaFXMain extends Application
{
    private SiemensPLCThread plcThread;
    private ModbusTCPThread modbusTCPThread;
    private HMIManager hmiManager;

    private HMIStarter hmiStarter;

    private File saveFile;

    @Override
    public void start(Stage stage)
    {
        try
        {
            saveFile = new File(System.getProperty("user.dir"), "saves.json");
            saveFile.createNewFile();

            this.hmiStarter = new HMIStarter(saveFile);
            this.hmiStarter.startEditingEngine();
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while Starting Main", exception, this);
        }
    }

    @Override
    public void stop() throws Exception
    {
        if(hmiStarter != null)
        {
            hmiStarter.stop();
        }
    }
}
