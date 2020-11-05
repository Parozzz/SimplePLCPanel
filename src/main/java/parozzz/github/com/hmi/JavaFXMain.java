package parozzz.github.com.hmi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.hmi.util.FXUtil;
import parozzz.github.com.logger.MainLogger;

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

    private File saveFile;

    @Override
    public void start(Stage stage)
    {
        try
        {
            saveFile = new File(System.getProperty("user.dir"), "saves.json");
            saveFile.createNewFile();

            plcThread = new SiemensPLCThread();
            plcThread.start();

            modbusTCPThread = new ModbusTCPThread();
            modbusTCPThread.start();

            hmiManager = new HMIManager(plcThread, modbusTCPThread);
            hmiManager.setup();
            if (saveFile.exists())
            {
                var jsonParser = new JSONParser();

                try (var fileReader = new FileReader(saveFile))
                {
                    var object = jsonParser.parse(fileReader);
                    if (object instanceof JSONObject)
                    {
                        var jsonDataMap = new JSONDataMap((JSONObject) object);
                        hmiManager.deserialize(jsonDataMap);
                    } else
                    {
                        hmiManager.setDefault();
                    }
                } catch (ParseException exception)
                {
                    hmiManager.setDefault();

                    MainLogger.getInstance().error("Error while Parsing Saved Data", exception, this);
                }
            }
            hmiManager.setupComplete();
            hmiManager.showStage(); //Show stage after everything has loaded

            FXUtil.runEveryMillis(1, hmiManager::loop);
            FXUtil.runEverySecond(60, this::saveData);

            //Stop the JavaFX platform is no windows are open
            Platform.setImplicitExit(true);
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while Starting Main", exception, this);
        }
    }

    @Override
    public void stop()
    {
        try
        {
            this.saveData();

            plcThread.setStop();
            modbusTCPThread.setStop();
            MainLogger.getInstance().setStop();

            plcThread.join(); //Waits for the thread to stop
            modbusTCPThread.join();
            MainLogger.getInstance().join();
        }catch (Exception exception) {
            MainLogger.getInstance().error("Error while stopping JavaFX Main Application", exception, this);
            return;
        }

        System.exit(0);
    }

    private void saveData()
    {
        try
        {
            var copiedFile = new File(saveFile.getParent(), "tmpsavefile.json");
            //Delete it in case it already exists
            if (copiedFile.exists())
            {
                copiedFile.delete();
            }

            Files.copy(saveFile.toPath(), copiedFile.toPath());

            var fileWriter = new FileWriter(copiedFile);
            hmiManager.serialize().getJson().writeJSONString(fileWriter);
            fileWriter.flush();
            fileWriter.close();

            Files.copy(copiedFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            copiedFile.delete();
        } catch (IOException exception)
        {
            MainLogger.getInstance().error("Error while Saving Data", exception, this);
        }
    }
}
