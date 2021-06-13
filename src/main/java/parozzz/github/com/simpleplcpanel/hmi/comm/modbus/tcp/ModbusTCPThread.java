package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusReadNumberIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusWriteNumberIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit.ModbusReadBitIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit.ModbusWriteBitIntermediate;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ModbusTCPThread extends CommunicationThread<ModbusTCPConnectionParams>
{
    private final TCPMasterConnection masterConnection;

    private final Set<ModbusReadNumberIntermediate> readHoldingRegisterSet;
    private final Set<ModbusWriteNumberIntermediate> writeHoldingRegisterSet;

    private final Set<ModbusReadBitIntermediate> readCoilSet;
    private final Set<ModbusWriteBitIntermediate> writeCoilSet;

    private final Set<ModbusReadBitIntermediate> readDiscreteInputsSet;
    private final Set<ModbusReadNumberIntermediate> readInputRegistersSet;

    public ModbusTCPThread(ModbusTCPCommunicationManager communicationManager)
    {
        super("ModbusTCPThread", communicationManager);

        this.readHoldingRegisterSet = ConcurrentHashMap.newKeySet();
        this.writeHoldingRegisterSet = ConcurrentHashMap.newKeySet();

        this.readCoilSet = ConcurrentHashMap.newKeySet();
        this.writeCoilSet = ConcurrentHashMap.newKeySet();

        this.readDiscreteInputsSet = ConcurrentHashMap.newKeySet();
        this.readInputRegistersSet = ConcurrentHashMap.newKeySet();

        this.masterConnection = new TCPMasterConnection(null); //the connection
        masterConnection.setTimeout(1000);
    }

    public Set<ModbusReadNumberIntermediate> getReadHoldingRegisterSet()
    {
        return readHoldingRegisterSet;
    }

    public Set<ModbusWriteNumberIntermediate> getWriteHoldingRegisterSet()
    {
        return writeHoldingRegisterSet;
    }

    public Set<ModbusReadBitIntermediate> getReadCoilSet()
    {
        return readCoilSet;
    }

    public Set<ModbusWriteBitIntermediate> getWriteCoilSet()
    {
        return writeCoilSet;
    }

    public Set<ModbusReadBitIntermediate> getReadDiscreteInputsSet()
    {
        return readDiscreteInputsSet;
    }

    public Set<ModbusReadNumberIntermediate> getReadInputRegistersSet()
    {
        return readInputRegistersSet;
    }

    @Override
    public synchronized void disconnect()
    {
        masterConnection.close();
    }

    @Override
    public synchronized boolean isConnected()
    {
        return masterConnection.isConnected();
    }

    @Override
    public void updateConnectionParams()
    {
        if(masterConnection.isConnected())
        {
            masterConnection.close();
        }

        if(communicationParams != null)
        {
            try
            {
                communicationParams.updateParams(masterConnection);
            }
            catch(UnknownHostException exception)
            {
                MainLogger.getInstance().error("Error while trying to change params of Modbus Client", exception, this);
            }
        }
    }

    @Override
    public boolean connect()
    {
        if(masterConnection.getAddress() == null)
        {
            return false;
        }

        if(masterConnection.isConnected())
        {
            return true;
        }

        try
        {
            masterConnection.connect();
        }
        catch(ConnectException connectException)
        {
            MainLogger.getInstance().info("Can not connect to ModbusTCP Server", this);
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().warning("Error while trying to connect to ModbusTCP Server", exception, this);
        }

        return masterConnection.isConnected();
    }

    @Override
    public void update()
    {
        try
        {
            var transaction = new ModbusTCPTransaction();
            transaction.setRetries(2);
            transaction.setConnection(masterConnection);

            this.manageIntermediateSet(readHoldingRegisterSet,
                    intermediateListMap -> this.readMultipleConsecutiveHoldings(transaction, intermediateListMap));
            this.manageIntermediateSet(writeHoldingRegisterSet,
                    intermediateListMap -> this.writeMultipleConsecutiveHoldings(transaction, intermediateListMap));

            this.manageIntermediateSet(readCoilSet,
                    intermediateListMap -> this.readMultipleCoils(transaction, intermediateListMap));
            this.manageIntermediateSet(writeCoilSet,
                    intermediateListMap -> this.writeMultipleConsecutiveCoils(transaction, intermediateListMap));

            this.manageIntermediateSet(readDiscreteInputsSet,
                    intermediateListMap -> this.readConsecutiveDiscreteInputs(transaction, intermediateListMap));
            this.manageIntermediateSet(readInputRegistersSet, intermediateListMap -> this
                    .readMultipleConsecutiveInputRegisters(transaction, intermediateListMap));
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while managing lists the modbus server", exception, this);
        }
    }

    private <T extends ModbusIntermediate> void manageIntermediateSet(Set<T> intermediateSet,
            ExceptionConsumer<Map<Integer, List<T>>> parseConsecutiveIntermediateConsumer) throws Exception
    {
        if(intermediateSet.isEmpty() || !this.isConnected())
        {
            return;
        }

        Map<Integer, List<T>> intermediateMap = new TreeMap<>();
        for(var intermediate : intermediateSet)
        {
            for(var offset : intermediate.getOffsetArray())
            {
                intermediateMap.computeIfAbsent(offset, tOffset -> new ArrayList<>()).add(intermediate);
            }
        }

        Map<Integer, List<T>> consecutiveIntermediateListMap = new TreeMap<>();

        int lastOffset = -1;
        for(var entry : intermediateMap.entrySet())
        {
            var offset = entry.getKey();
            var intermediateList = entry.getValue();

            if(lastOffset != -1 && lastOffset + 1 != offset)
            {
                if(!consecutiveIntermediateListMap.isEmpty())
                {
                    parseConsecutiveIntermediateConsumer.accept(consecutiveIntermediateListMap);
                    consecutiveIntermediateListMap.clear();
                }
            }

            lastOffset = offset;
            consecutiveIntermediateListMap.put(offset, intermediateList);
        }

        if(!consecutiveIntermediateListMap.isEmpty())
        {
            parseConsecutiveIntermediateConsumer.accept(consecutiveIntermediateListMap);
        }
    }

    private void writeMultipleConsecutiveHoldings(ModbusTransaction transaction,
            Map<Integer, List<ModbusWriteNumberIntermediate>> intermediateListMap)
    {
        try
        {
            var registerList = new ArrayList<SimpleInputRegister>();

            var firstOffset = -1;
            for(var entry : intermediateListMap.entrySet())
            {
                var offset = entry.getKey();
                var intermediateList = entry.getValue();

                if(intermediateList.isEmpty())
                {
                    throw new IllegalStateException(
                            "Found an intermediate sub list that is empty while writing holdings");
                }

                if(intermediateList.size() > 1)
                {
                    MainLogger.getInstance()
                            .warning("Trying to write multiple holdings to the same offset " + offset, this);
                }

                if(firstOffset == -1)
                {
                    firstOffset = offset;
                }

                var intermediate = intermediateList.get(0);
                registerList.add(new SimpleInputRegister(intermediate.getNextWord()));
            }

            //Get the first intermediate offset and then set the based on the size
            var request = new WriteMultipleRegistersRequest(firstOffset,
                    registerList.toArray(SimpleInputRegister[]::new));
            transaction.setRequest(request);
            transaction.execute();

            //var response = (WriteMultipleRegistersResponse) transaction.getResponse();
            //Check something here ??
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp holding registers", exception, this);
            this.disconnect();
        }

    }

    private void readMultipleConsecutiveHoldings(ModbusTransaction transaction,
            Map<Integer, List<ModbusReadNumberIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            //Get the lowest (And it should be FIRST) intermediate offset and then set the based on the size
            var request = new ReadMultipleRegistersRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);
            transaction.execute();

            var response = (ReadMultipleRegistersResponse) transaction.getResponse();

            var x = 0;
            for(var intermediateList : intermediateListMap.values())
            {
                var register = response.getRegisters()[x++];
                for(var intermediate : intermediateList)
                {
                    intermediate.setNextWord(register.getValue());
                }
            }
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp holding registers", exception, this);
            this.disconnect();
        }
    }

    private void writeMultipleConsecutiveCoils(ModbusTransaction transaction,
            Map<Integer, List<ModbusWriteBitIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            var request = new WriteMultipleCoilsRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);

            var x = 0;
            for(var entry : intermediateListMap.entrySet())
            {
                var intermediateList = entry.getValue();
                if(intermediateList.isEmpty())
                {
                    throw new IllegalStateException(
                            "Found an intermediate sub list that is empty while writing multiple coils");
                }

                if(intermediateList.size() > 1)
                {
                    MainLogger.getInstance()
                            .warning("Trying to write multiple coils to the same offset " + entry.getKey(), this);
                }

                var intermediate = intermediateList.get(0);
                request.setCoilStatus(x++, intermediate.getValue());
            }

            transaction.execute();

            //var response = (WriteMultipleCoilsResponse) transaction.getResponse();
            //???
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while writing Modbus tcp multiple coils", exception, this);
            this.disconnect();
        }
    }

    private void readMultipleCoils(ModbusTransaction transaction,
            Map<Integer, List<ModbusReadBitIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            //Get the first intermediate offset and then set the based on the size
            var request = new ReadCoilsRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);
            transaction.execute();

            var response = (ReadCoilsResponse) transaction.getResponse();

            var x = 0;
            for(var intermediateList : intermediateListMap.values())
            {
                var coilBit = response.getCoils().getBit(x++);
                for(var intermediate : intermediateList)
                {
                    intermediate.setValue(coilBit);
                }
            }
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp multiple coils", exception, this);
            this.disconnect();
        }
    }

    private void readConsecutiveDiscreteInputs(ModbusTransaction transaction,
            Map<Integer, List<ModbusReadBitIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            //Get the first intermediate offset and then set the based on the size
            var request = new ReadInputDiscretesRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);
            transaction.execute();

            var response = (ReadInputDiscretesResponse) transaction.getResponse();

            var x = 0;
            for(var intermediateList : intermediateListMap.values())
            {
                var discreteBit = response.getDiscretes().getBit(x++);
                for(var intermediate : intermediateList)
                {
                    intermediate.setValue(discreteBit);
                }
            }
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp discrete inputs", exception, this);
            this.disconnect();
        }
    }

    private void readMultipleConsecutiveInputRegisters(ModbusTransaction transaction,
            Map<Integer, List<ModbusReadNumberIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            //Get the first intermediate offset and then set the based on the size
            var request = new ReadInputRegistersRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);
            transaction.execute();

            var response = (ReadInputRegistersResponse) transaction.getResponse();

            int x = 0;
            for(var intermediateList : intermediateListMap.values())
            {
                var register = response.getRegisters()[x++];
                for(var intermediate : intermediateList)
                {
                    intermediate.setNextWord(register.getValue());
                }
            }
        }
        catch(Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp input registers", exception, this);
            this.disconnect();
        }
    }

    @FunctionalInterface
    private interface ExceptionConsumer<T>
    {
        void accept(T t) throws Exception;
    }
}
