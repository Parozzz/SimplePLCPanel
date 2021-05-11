package parozzz.github.com.simpleplcpanel.hmi;

import javafx.application.Application;
import javafx.stage.Stage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagStage;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class JavaFXMain extends Application
{
    private SiemensS7Thread plcThread;
    private ModbusTCPThread modbusTCPThread;
    private HMIManager hmiManager;

    private HMIStarter hmiStarter;

    private File saveFile;

    @Override
    public void start(Stage stage)
    {
        try
        {
            saveFile = this.getSaveFile();

            this.hmiStarter = new HMIStarter(saveFile);
            this.hmiStarter.startEditingEngine();
/*
            var tabTableViewTest = new TagTableViewTest();
            tabTableViewTest.setup();
            tabTableViewTest.showStage();
*/
            var tagStage = new TagStage(hmiStarter.getHmiManager().getCommunicationDataHolder());
            tagStage.setup();
            tagStage.showStage();
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

    private File getSaveFile() throws IOException
    {
        var userDir = System.getProperty("user.dir");

        var saveFile = new File(userDir, "saves.json");
        saveFile.createNewFile();

        var saveFileBackupDir = new File(userDir, "Backups");
        saveFileBackupDir.mkdirs();

        var sdf = new SimpleDateFormat(); // creo l'oggetto
        sdf.applyPattern("yyyy.MM.dd.hh.mm.ss");

        var stringDate = sdf.format(new Date());
        var backupSaveFile = new File(saveFileBackupDir, "bak" + stringDate + ".json");

        Files.copy(saveFile.toPath(), backupSaveFile.toPath());

        return saveFile;
    }
}
