package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensPLCThread;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final SiemensPLCThread plcThread;
    private final MainEditStage mainEditStage;

    public HMIManager(SiemensPLCThread plcThread, ModbusTCPThread modbusTCPThread, Runnable saveDataRunnable) throws IOException
    {
        this.plcThread = plcThread;

        super.addFXChild(this.mainEditStage = new MainEditStage(plcThread, modbusTCPThread, saveDataRunnable));
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
        mainEditStage.showStage();
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
