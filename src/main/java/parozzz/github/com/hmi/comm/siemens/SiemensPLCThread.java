package parozzz.github.com.hmi.comm.siemens;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.rwdata.SiemensS7ReadData;
import parozzz.github.com.PLC.siemens.rwdata.SiemensS7WriteData;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.hmi.comm.CommThread;
import parozzz.github.com.hmi.comm.siemens.intermediate.SiemensPLCReadableWrappedDataIntermediate;
import parozzz.github.com.hmi.comm.siemens.intermediate.SiemensPLCWrappedDataIntermediate;
import parozzz.github.com.hmi.comm.siemens.intermediate.SiemensPLCWritableBitWrappedDataIntermediate;
import parozzz.github.com.hmi.comm.siemens.intermediate.SiemensPLCWritableWrappedDataIntermediate;
import parozzz.github.com.logger.Loggable;
import parozzz.github.com.logger.MainLogger;
import parozzz.github.com.util.concurrent.SettableConcurrentObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SiemensPLCThread extends CommThread implements Loggable
{
    private final Set<SiemensPLCReadableWrappedDataIntermediate<?>> readDataIntermediateSet;
    private final Set<SiemensPLCWritableWrappedDataIntermediate<?>> writeDataIntermediateSet;

    private final Set<SiemensPLCWritableBitWrappedDataIntermediate> writeBitWrapperSet;

    private SiemensS7Client client;
    private volatile String ipAddress;
    private volatile int rack;
    private volatile int slot;
    private volatile boolean newConnectionParams = false;
    private volatile boolean firstConnectionParamsReceived = false;

    private SettableConcurrentObject<String> queryModelNumberObject;

    public SiemensPLCThread()
    {
        this.setName("SiemensPLCThread");

        writeBitWrapperSet = ConcurrentHashMap.newKeySet();
        readDataIntermediateSet = ConcurrentHashMap.newKeySet();
        writeDataIntermediateSet = ConcurrentHashMap.newKeySet();
    }

    public Set<SiemensPLCReadableWrappedDataIntermediate<?>> getReadDataIntermediateSet()
    {
        return readDataIntermediateSet;
    }

    public Set<SiemensPLCWritableBitWrappedDataIntermediate> getWriteBitWrapperSet()
    {
        return writeBitWrapperSet;
    }

    public Set<SiemensPLCWritableWrappedDataIntermediate<?>> getWriteDataIntermediateSet()
    {
        return writeDataIntermediateSet;
    }

    public synchronized void setConnectionParameters(String ipAddress, int rack, int slot)
    {
        this.ipAddress = ipAddress;
        this.rack = rack;
        this.slot = slot;

        newConnectionParams = true;
        firstConnectionParamsReceived = true;
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
    public void loop() throws InterruptedException
    {
        while (!firstConnectionParamsReceived)
        {
            Thread.sleep(250);
        }

        try
        {
            if (!connect())
            {
                Thread.onSpinWait();
                return;
            }
        } catch (SiemensS7Error.SiemensS7Exception exception)
        {
            if (exception.getError() == SiemensS7Error.TCPConnectionFailed)
            {
                MainLogger.getInstance().info("Cannot connect to Siemens PLC", this);
            } else
            {
                MainLogger.getInstance().warning("Error while connecting to Siemens PLC", exception, this);
            }
        }

        if(!isConnected())
        {
            Thread.sleep(10000);
            return;
        }

        if (queryModelNumberObject != null)
        {
            try
            {
                var modelString = client.getModelInfo().getModel();
                queryModelNumberObject.setObject(modelString);
                queryModelNumberObject = null;
            } catch (SiemensS7Error.SiemensS7Exception exception)
            {
                MainLogger.getInstance().warning("Error while querying Siemens PLC model number", exception, this);
            }
        }

        if (!update)
        {
            Thread.sleep(50);
            return;
        }

        //This needs to be handled locally, because of the update to reset
        try
        {
            if (!(readDataIntermediateSet.isEmpty() && writeBitWrapperSet.isEmpty() && writeDataIntermediateSet.isEmpty()))
            {
                //Firstly write the value and then read it in case it has not changed and avoid delay between click and display
                var dbWritingMap = this.splitByDBNumber(writeDataIntermediateSet);
                for (var entry : dbWritingMap.entrySet())
                {
                    var dbNumber = entry.getKey();
                    var writeData = new SiemensS7WriteData(client, SiemensS7AreaType.DB, dbNumber);

                    var intermediateSet = entry.getValue();
                    for (var intermediate : intermediateSet)
                    {
                        writeData.append(intermediate.getS7WritableWrappedData());
                    }

                    writeData.write();
                }

                for (var bitIntermediate : writeBitWrapperSet)
                {
                    var state = bitIntermediate.getValue();
                    client.writeBit(bitIntermediate.getAreaType(), bitIntermediate.getDbNumber(),
                            bitIntermediate.getOffset(), bitIntermediate.getS7BitData().getBitOffset(),
                            state);
                }

                var dbReadingMap = this.splitByDBNumber(readDataIntermediateSet);
                for (var entry : dbReadingMap.entrySet())
                {
                    var dbNumber = entry.getKey();
                    var readData = new SiemensS7ReadData(client, SiemensS7AreaType.DB, dbNumber);

                    var intermediateSet = entry.getValue();
                    for (var intermediate : intermediateSet)
                    {
                        readData.append(intermediate.getS7ReadableWrappedData());
                    }

                    readData.read();
                }

                this.readDataOf(SiemensS7AreaType.INPUT);
                this.readDataOf(SiemensS7AreaType.OUTPUT);
                this.readDataOf(SiemensS7AreaType.MERKER);
            }
        } catch (SiemensS7Error.SiemensS7Exception exception)
        {
            MainLogger.getInstance().warning("Error while Reading/Writing data to Siemens PLC", exception, this);
        }

        update = false;
    }


    private boolean connect() throws SiemensS7Error.SiemensS7Exception
    {
        if (newConnectionParams)
        {
            if (client != null)
            {
                client.disconnect();
            }

            client = new SiemensS7Client(ipAddress, rack, slot);
            newConnectionParams = false;
        }

        if (client == null)
        {
            return false;
        }

        if (!client.isConnected())
        {
            client.connect();

            var timestamp = System.currentTimeMillis();
            while (!client.isConnected())
            {
                //If after 5 seconds is not connected, return!
                if ((System.currentTimeMillis() - timestamp) > 5000)
                {
                    return false;
                }
            }

            System.out.println("PLC Model: " + client.getModelInfo().getModel());
        }

        return true;
    }

    private void readDataOf(SiemensS7AreaType areaType) throws SiemensS7Error.SiemensS7Exception
    {
        boolean valid = false;

        var readData = new SiemensS7ReadData(client, areaType);
        for (var intermediate : readDataIntermediateSet)
        {
            if (intermediate.getAreaType() == areaType)
            {
                valid = true;
                readData.append(intermediate.getS7ReadableWrappedData());
            }
        }

        if (valid)
        {
            readData.read();
        }
    }

    private <T extends SiemensPLCWrappedDataIntermediate> Map<Integer, Set<T>> splitByDBNumber(Set<T> intermediateSet)
    {
        var dbReadingMap = new HashMap<Integer, Set<T>>();

        for (var intermediate : intermediateSet)
        {
            var dbNumber = intermediate.getDbNumber();
            if (intermediate.getAreaType() == SiemensS7AreaType.DB && dbNumber >= 1)
            {
                dbReadingMap.computeIfAbsent(dbNumber, t -> new HashSet<>()).add(intermediate);
            }
        }

        return dbReadingMap;
    }

    @Override
    public String log()
    {
        return "IPAddress: " + client.getIpAddress() +
                ", Rack: " + client.getRack() +
                ", Slot: " + client.getSlot();
    }
}
