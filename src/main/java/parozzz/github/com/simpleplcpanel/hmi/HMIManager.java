package parozzz.github.com.simpleplcpanel.hmi;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

import java.io.IOException;

public final class HMIManager extends FXController
{
    private final CommunicationDataHolder communicationDataHolder;
    private final MainEditStage mainEditStage;

    public HMIManager(CommunicationDataHolder communicationDataHolder, Runnable saveDataRunnable) throws IOException
    {
        super.addFXChild(this.communicationDataHolder = communicationDataHolder)
                .addFXChild(this.mainEditStage = new MainEditStage(communicationDataHolder, saveDataRunnable));
    }

    public CommunicationDataHolder getCommunicationDataHolder()
    {
        return communicationDataHolder;
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
