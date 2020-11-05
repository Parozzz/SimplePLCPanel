package parozzz.github.com.hmi;

import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final SiemensPLCThread plcThread;
    private final MainEditStage mainEditStage;

    public HMIManager(SiemensPLCThread plcThread, ModbusTCPThread modbusTCPThread) throws IOException
    {
        super("HMIManager");

        this.plcThread = plcThread;

        super.addFXChild(this.mainEditStage = new MainEditStage(plcThread, modbusTCPThread));
    }

    public SiemensPLCThread getPlcThread()
    {
        return plcThread;
    }

    public MainEditStage getMainEditStage()
    {
        return mainEditStage;
    }

    public void showStage()
    {
        mainEditStage.start();
    }

    @Override
    public JSONDataMap serialize()
    {
        return super.serialize();
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        super.deserialize(jsonDataMap);
    }
}
