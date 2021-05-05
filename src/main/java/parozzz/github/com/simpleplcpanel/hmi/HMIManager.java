package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final SiemensS7Thread plcThread;
    private final MainEditStage mainEditStage;

    public HMIManager(SiemensS7Thread plcThread, ModbusTCPThread modbusTCPThread, Runnable saveDataRunnable) throws IOException
    {
        this.plcThread = plcThread;

        super.addFXChild(this.mainEditStage = new MainEditStage(plcThread, modbusTCPThread, saveDataRunnable));
    }

    public SiemensS7Thread getPlcThread()
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
