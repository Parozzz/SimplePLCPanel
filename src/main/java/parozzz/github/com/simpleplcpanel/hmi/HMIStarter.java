package parozzz.github.com.simpleplcpanel.hmi;

import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class HMIStarter
{
    private final CommunicationDataHolder communicationDataHolder;
    private final HMIManager hmiManager;

    private final File saveFile;

    public HMIStarter(File saveFile) throws IOException
    {
        this.communicationDataHolder = new CommunicationDataHolder();
        this.hmiManager = new HMIManager(communicationDataHolder, this::saveData);

        this.saveFile = saveFile;
    }

    public HMIManager getHmiManager()
    {
        return hmiManager;
    }

    private void init()
    {
        try
        {
            for(var commThread : communicationDataHolder.getCommThreadCollection())
            {
                commThread.start();
            }

            hmiManager.setup();
            if(saveFile.exists())
            {
                var jsonParser = new JSONParser();

                try(var fileReader = new FileReader(saveFile))
                {
                    var object = jsonParser.parse(fileReader);
                    if(object instanceof JSONObject)
                    {
                        var jsonDataMap = new JSONDataMap((JSONObject) object);
                        hmiManager.deserialize(jsonDataMap);
                    }else
                    {
                        hmiManager.setDefault();
                    }
                }
                catch(ParseException exception)
                {
                    hmiManager.setDefault();

                    MainLogger.getInstance().error("Error while Parsing Saved Data", exception, this);
                }
            }
            hmiManager.setupComplete();

            FXUtil.runEveryMillis(1, hmiManager::loop);
            //FXUtil.runEverySecond(60, this::saveData);

            //Stop the JavaFX platform is no windows are open
            Platform.setImplicitExit(true);
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while Starting Main", exception, this);
        }
    }

    public void startEditingEngine()
    {
        init();
        hmiManager.showStage(); //Show stage after everything has loaded
    }

    public void startRuntimeEngine()
    {
        init();
        hmiManager.getMainEditStage().showRuntimeScene(false, false);
    }

    public void stop()
    {
        try
        {
            this.saveData();

            for(var commThread : communicationDataHolder.getCommThreadCollection())
            {
                commThread.setStop();
            }
            MainLogger.getInstance().setStop();

            //Waits for all the threads to stop
            for(var commThread : communicationDataHolder.getCommThreadCollection())
            {
                commThread.join();
            }
            MainLogger.getInstance().join();
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while stopping JavaFX Main Application", exception, this);
            return;
        }

        System.exit(0);
    }

    public void saveData()
    {
        try
        {
            var copiedFile = new File(saveFile.getParent(), "tmpsavefile.json");
            //Delete it in case it already exists
            if(copiedFile.exists())
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
        }
        catch(IOException exception)
        {
            MainLogger.getInstance().error("Error while Saving Data", exception, this);
        }
    }
}
