package parozzz.github.com.simpleplcpanel.hmi.comm.siemens;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.SiemensS7ReadData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.SiemensS7WriteData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7ReadableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableBitWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;
import parozzz.github.com.simpleplcpanel.util.concurrent.SettableConcurrentObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SiemensS7Thread extends CommThread<SiemensS7ConnectionParams>
{
    private final Set<SiemensS7ReadableWrappedDataIntermediate<?>> readDataIntermediateSet;
    private final Set<SiemensS7WritableWrappedDataIntermediate<?>> writeDataIntermediateSet;

    private final Set<SiemensS7WritableBitWrappedDataIntermediate> writeBitWrapperSet;

    private SiemensS7Client client;

    private SettableConcurrentObject<String> queryModelNumberObject;

    public SiemensS7Thread()
    {
        this.setName("SiemensPLCThread");

        writeBitWrapperSet = ConcurrentHashMap.newKeySet();
        readDataIntermediateSet = ConcurrentHashMap.newKeySet();
        writeDataIntermediateSet = ConcurrentHashMap.newKeySet();
    }

    public Set<SiemensS7ReadableWrappedDataIntermediate<?>> getReadDataIntermediateSet()
    {
        return readDataIntermediateSet;
    }

    public Set<SiemensS7WritableBitWrappedDataIntermediate> getWriteBitWrapperSet()
    {
        return writeBitWrapperSet;
    }

    public Set<SiemensS7WritableWrappedDataIntermediate<?>> getWriteDataIntermediateSet()
    {
        return writeDataIntermediateSet;
    }

    @Override
    public synchronized boolean isConnected()
    {
        return client != null && client.isConnected();
    }

    @Override
    public synchronized void disconnect()
    {
        client.disconnect();
    }

    public void queryModel(SettableConcurrentObject<String> object)
    {
        queryModelNumberObject = object;
    }

    @Override
    public void updateConnectionParams()
    {
        if(client != null && client.isConnected())
        {
            client.disconnect();
        }

        client = null;
        if(communicationParams != null)
        {
            client = communicationParams.createClient();
        }
    }

    @Override
    public boolean connect()
    {
        if(client == null)
        {
            return false;
        }

        if(client.isConnected())
        {
            return true;
        }

        try
        {
            client.connect();

            var model = client.getModelInfo().getModel();
            MainLogger.getInstance().info("PLC Model: " + model, this);

            return true;
        }
        catch(SiemensS7Error.SiemensS7Exception exception)
        {
            if(exception.getError() == SiemensS7Error.TCPConnectionFailed)
            {
                MainLogger.getInstance().info("Cannot connect to Siemens PLC", this);
            }else
            {
                MainLogger.getInstance().warning("Error while connecting to Siemens PLC", exception, this);
            }

            return false;
        }
    }

    @Override
    public void update()
    {
        if(queryModelNumberObject != null)
        {
            try
            {
                var modelString = client.getModelInfo().getModel();
                queryModelNumberObject.setObject(modelString);
                queryModelNumberObject = null;
            }
            catch(SiemensS7Error.SiemensS7Exception exception)
            {
                MainLogger.getInstance().warning("Error while querying Siemens PLC model number", exception, this);
            }
        }

        //This needs to be handled locally, because of the update to reset
        try
        {
            if(!(readDataIntermediateSet.isEmpty() && writeBitWrapperSet.isEmpty() && writeDataIntermediateSet.isEmpty()))
            {
                //Firstly write the value and then read it in case it has not changed and avoid delay between click and display
                var dbWritingMap = this.splitByDBNumber(writeDataIntermediateSet);
                for(var entry : dbWritingMap.entrySet())
                {
                    var dbNumber = entry.getKey();
                    var writeData = new SiemensS7WriteData(client, SiemensS7AreaType.DB, dbNumber);

                    var intermediateSet = entry.getValue();
                    for(var intermediate : intermediateSet)
                    {
                        writeData.append(intermediate.getS7WritableWrappedData());
                    }

                    writeData.write();
                }

                for(var bitIntermediate : writeBitWrapperSet)
                {
                    var state = bitIntermediate.getValue();
                    client.writeBit(bitIntermediate.getAreaType(), bitIntermediate.getDbNumber(),
                            bitIntermediate.getOffset(), bitIntermediate.getS7BitData().getBitOffset(),
                            state);
                }

                var dbReadingMap = this.splitByDBNumber(readDataIntermediateSet);
                for(var entry : dbReadingMap.entrySet())
                {
                    var dbNumber = entry.getKey();
                    var readData = new SiemensS7ReadData(client, SiemensS7AreaType.DB, dbNumber);

                    var intermediateSet = entry.getValue();
                    for(var intermediate : intermediateSet)
                    {
                        readData.append(intermediate.getS7ReadableWrappedData());
                    }

                    readData.read();
                }

                this.readDataOf(SiemensS7AreaType.INPUT);
                this.readDataOf(SiemensS7AreaType.OUTPUT);
                this.readDataOf(SiemensS7AreaType.MERKER);
            }
        }
        catch(SiemensS7Error.SiemensS7Exception exception)
        {
            MainLogger.getInstance().warning("Error while Reading/Writing data to Siemens PLC", exception, this);
        }
    }

    private void readDataOf(SiemensS7AreaType areaType) throws SiemensS7Error.SiemensS7Exception
    {
        boolean valid = false;

        var readData = new SiemensS7ReadData(client, areaType);
        for(var intermediate : readDataIntermediateSet)
        {
            if(intermediate.getAreaType() == areaType)
            {
                valid = true;
                readData.append(intermediate.getS7ReadableWrappedData());
            }
        }

        if(valid)
        {
            readData.read();
        }
    }

    private <T extends SiemensS7WrappedDataIntermediate> Map<Integer, Set<T>> splitByDBNumber(Set<T> intermediateSet)
    {
        var dbReadingMap = new HashMap<Integer, Set<T>>();

        for(var intermediate : intermediateSet)
        {
            var dbNumber = intermediate.getDbNumber();
            if(intermediate.getAreaType() == SiemensS7AreaType.DB && dbNumber >= 1)
            {
                dbReadingMap.computeIfAbsent(dbNumber, t -> new HashSet<>()).add(intermediate);
            }
        }

        return dbReadingMap;
    }
}
