package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPCommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7CommunicationManager;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class CommunicationDataHolder extends FXController
{
    private final Map<CommunicationType<?>, CommunicationThread<?>> commThreadMap;
    private final Map<CommunicationType<?>, NetworkCommunicationManager<?>> communicationManagerMap;

    private final CommunicationStage communicationStage;

    public CommunicationDataHolder() throws IOException
    {
        this.commThreadMap = new HashMap<>();
        this.communicationManagerMap = new HashMap<>();

        this.fillMaps(CommunicationType.SIEMENS_S7, SiemensS7Thread::new, new SiemensS7CommunicationManager())
                .fillMaps(CommunicationType.MODBUS_TCP, ModbusTCPThread::new, new ModbusTCPCommunicationManager());

        this.addFXChild(this.communicationStage = new CommunicationStage(this));
    }

    public CommunicationStage getCommunicationStage()
    {
        return communicationStage;
    }

    public CommunicationType<?> getCurrentCommunicationType()
    {
        return communicationStage.getCommunicationType();
    }

    /*
        ====================
        COMMUNICATION THREAD
        ====================
    */
    public CommunicationThread<?> getCommThread(CommunicationType<?> communicationType)
    {
        return commThreadMap.get(communicationType);
    }

    public <T extends CommunicationThread<?>> T getCommThread(CommunicationType<?> communicationType, Class<T> tClass)
    {
        var commThread = this.getCommThread(communicationType);
        return tClass.isInstance(commThread) ? tClass.cast(commThread) : null;
    }

    public CommunicationThread<?> getCurrentCommThread()
    {
        return this.getCommThread(communicationStage.getCommunicationType());
    }

    public <CM extends CommunicationThread<?>> CM getCurrentCommThread(Class<CM> cmClass)
    {
        return this.getCommThread(communicationStage.getCommunicationType(), cmClass);
    }

    public Collection<CommunicationThread<?>> getCommThreadCollection()
    {
        return Set.copyOf(commThreadMap.values());
    }

    /*
        =====================
        =====================
    */
    /*
        =====================
        COMMUNICATION MANAGER
        =====================
    */
    public NetworkCommunicationManager<?> getCommunicationManager(CommunicationType<?> communicationType)
    {
        return communicationManagerMap.get(communicationType);
    }

    public <CM extends NetworkCommunicationManager<?>> CM getCommunicationManager(
            CommunicationType<?> communicationType,
            Class<CM> cmClass)
    {
        var communicationManager = this.getCommunicationManager(communicationType);
        return cmClass.isInstance(communicationManager) ? cmClass.cast(communicationManager) : null;
    }

    public NetworkCommunicationManager<?> getCurrentCommunicationManager()
    {
        return this.getCommunicationManager(communicationStage.getCommunicationType());
    }

    public <CM extends NetworkCommunicationManager<?>> CM getCurrentCommunicationManager(Class<CM> cmClass)
    {
        return this.getCommunicationManager(communicationStage.getCommunicationType(), cmClass);
    }

    /*
        =====================
        =====================
    */

    private <P extends CommunicationConnectionParams,
            CM extends NetworkCommunicationManager<P>> CommunicationDataHolder fillMaps(
            CommunicationType<?> communicationType, Function<CM, CommunicationThread<P>> communicationThreadFunction,
            CM communicationManager)
    {
        var communicationThread = communicationThreadFunction.apply(communicationManager);
        commThreadMap.put(communicationType, communicationThread);
        communicationManagerMap.put(communicationType, communicationManager);

        this.addMultipleFXChild(communicationThread, communicationManager);
        return this;
    }
    
}
