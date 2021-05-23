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

public final class CommunicationDataHolder extends FXController
{
    private final Map<CommunicationType, CommThread<?>> commThreadMap;
    private final Map<CommunicationType, NetworkCommunicationManager<?>> communicationManagerMap;

    private final CommunicationStage communicationStage;

    public CommunicationDataHolder() throws IOException
    {
        this.commThreadMap = new HashMap<>();
        this.communicationManagerMap = new HashMap<>();

        this.fillMaps(CommunicationType.SIEMENS_S7, new SiemensS7Thread(), SiemensS7CommunicationManager::new)
                .fillMaps(CommunicationType.MODBUS_TCP, new ModbusTCPThread(), ModbusTCPCommunicationManager::new);

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
    public CommThread<?> getCommThread(CommunicationType<?> communicationType)
    {
        return commThreadMap.get(communicationType);
    }

    public <T extends CommThread<?>> T getCommThread(CommunicationType<?> communicationType, Class<T> tClass)
    {
        var commThread = this.getCommThread(communicationType);
        return tClass.isInstance(commThread) ? tClass.cast(commThread) : null;
    }

    public CommThread<?> getCurrentCommThread()
    {
        return this.getCommThread(communicationStage.getCommunicationType());
    }

    public <CM extends CommThread<?>> CM getCurrentCommThread(Class<CM> cmClass)
    {
        return this.getCommThread(communicationStage.getCommunicationType(), cmClass);
    }

    public Collection<CommThread<?>> getCommThreadCollection()
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
    public NetworkCommunicationManager<?> getCommunicationManager(CommunicationType communicationType)
    {
        return communicationManagerMap.get(communicationType);
    }

    public <CM extends NetworkCommunicationManager<?>> CM getCommunicationManager(
            CommunicationType communicationType,
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

    private <CM extends CommThread<?>> CommunicationDataHolder fillMaps(
            CommunicationType communicationType, CM commThread,
            CommunicationManagerFunction<CM> communicationManagerFunction) throws IOException
    {
        commThreadMap.put(communicationType, commThread);
        var communicationManager = communicationManagerFunction.accept(commThread);
        if (communicationManager != null)
        {
            this.addFXChild(communicationManager);
            communicationManagerMap.put(communicationType, communicationManager);
        }

        return this;
    }


    private interface CommunicationManagerFunction<CM extends CommThread<?>>
    {
        NetworkCommunicationManager<CM> accept(CM commThread) throws IOException;
    }
}
