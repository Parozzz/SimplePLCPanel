package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.ModbusTCPIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.ModbusTCPReadNumberIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.ModbusTCPWriteNumberIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.bit.ModbusTCPReadBitIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.bit.ModbusTCPWriteBitIntermediate;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ModbusTCPThread extends CommThread implements Loggable
{
    private final TCPMasterConnection masterConnection;

    private final Set<ModbusTCPReadNumberIntermediate> readHoldingRegisterSet;
    private final Set<ModbusTCPWriteNumberIntermediate> writeHoldingRegisterSet;

    private final Set<ModbusTCPReadBitIntermediate> readCoilSet;
    private final Set<ModbusTCPWriteBitIntermediate> writeCoilSet;

    private final Set<ModbusTCPReadBitIntermediate> readDiscreteInputsSet;
    private final Set<ModbusTCPReadNumberIntermediate> readInputRegistersSet;

    private volatile String ipAddress;
    private volatile int port;
    private volatile boolean newConnectionParams;
    private boolean firstConnectionParamsReceived;

    public ModbusTCPThread()
    {
        this.setName("ModbusTCPThread");

        this.readHoldingRegisterSet = ConcurrentHashMap.newKeySet();
        this.writeHoldingRegisterSet = ConcurrentHashMap.newKeySet();

        this.readCoilSet = ConcurrentHashMap.newKeySet();
        this.writeCoilSet = ConcurrentHashMap.newKeySet();

        this.readDiscreteInputsSet = ConcurrentHashMap.newKeySet();
        this.readInputRegistersSet = ConcurrentHashMap.newKeySet();

        this.masterConnection = new TCPMasterConnection(null); //the connection
        masterConnection.setTimeout(1000);
    }

    public Set<ModbusTCPReadNumberIntermediate> getReadHoldingRegisterSet()
    {
        return readHoldingRegisterSet;
    }

    public Set<ModbusTCPWriteNumberIntermediate> getWriteHoldingRegisterSet()
    {
        return writeHoldingRegisterSet;
    }

    public Set<ModbusTCPReadBitIntermediate> getReadCoilSet()
    {
        return readCoilSet;
    }

    public Set<ModbusTCPWriteBitIntermediate> getWriteCoilSet()
    {
        return writeCoilSet;
    }

    public Set<ModbusTCPReadBitIntermediate> getReadDiscreteInputsSet()
    {
        return readDiscreteInputsSet;
    }

    public Set<ModbusTCPReadNumberIntermediate> getReadInputRegistersSet()
    {
        return readInputRegistersSet;
    }

    @Override
    public synchronized void disconnect()
    {
        masterConnection.close();
    }

    public synchronized void setConnectionParameters(String ipAddress, int port)
    {
        this.ipAddress = ipAddress;
        this.port = port;

        newConnectionParams = true;
        firstConnectionParamsReceived = true;
    }

    @Override
    public synchronized boolean isConnected()
    {
        return masterConnection.isConnected();
    }

    @Override
    public void loop() throws InterruptedException
    {
        while (!firstConnectionParamsReceived)
        {
            Thread.sleep(250);
        }

        if (newConnectionParams)
        {
            if (masterConnection.isConnected())
            {
                masterConnection.close();
            }

            try
            {
                masterConnection.setAddress(InetAddress.getByName(ipAddress));
                masterConnection.setPort(port);
            } catch (UnknownHostException exception)
            {
                MainLogger.getInstance().error("Error while trying to change params of Modbus Client", exception, this);
            }

            newConnectionParams = false;
        }

        if (masterConnection.getAddress() == null)
        {
            return;
        }

        if (!masterConnection.isConnected())
        {
            try
            {
                masterConnection.connect();
            } catch (Exception exception)
            {
                MainLogger.getInstance().error("Error while trying to connect to Modbus Server", exception, this);
            }

            if (!masterConnection.isConnected())
            {
                this.sleepWithStopCheck(10);
                return;
            }
        }

        if (!update)
        {
            Thread.sleep(50);
            return;
        }

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
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while managing lists the modbus server", exception, this);
        }

        update = false;
    }

    @Override public String log()
    {
        return "IPAddress " + masterConnection.getAddress().getHostAddress()
                + ", Port " + masterConnection.getPort()
                + ", Timeout " + masterConnection.getTimeout();
    }

    private <T extends ModbusTCPIntermediate> void manageIntermediateSet(Set<T> intermediateSet,
            ExceptionConsumer<Map<Integer, List<T>>> parseConsecutiveIntermediateConsumer) throws Exception
    {
        if (intermediateSet.isEmpty() || !this.isConnected())
        {
            return;
        }

        Map<Integer, List<T>> intermediateMap = new TreeMap<>();
        for (var intermediate : intermediateSet)
        {
            for (var offset : intermediate.getOffsetArray())
            {
                intermediateMap.computeIfAbsent(offset, tOffset -> new ArrayList<>()).add(intermediate);
            }
        }

        Map<Integer, List<T>> consecutiveIntermediateListMap = new TreeMap<>();

        int lastOffset = -1;
        for (var entry : intermediateMap.entrySet())
        {
            var offset = entry.getKey();
            var intermediateList = entry.getValue();

            if (lastOffset != -1 && lastOffset + 1 != offset)
            {
                if (!consecutiveIntermediateListMap.isEmpty())
                {
                    parseConsecutiveIntermediateConsumer.accept(consecutiveIntermediateListMap);
                    consecutiveIntermediateListMap.clear();
                }
            }

            lastOffset = offset;
            consecutiveIntermediateListMap.put(offset, intermediateList);
        }

        if (!consecutiveIntermediateListMap.isEmpty())
        {
            parseConsecutiveIntermediateConsumer.accept(consecutiveIntermediateListMap);
        }
    }

    private void writeMultipleConsecutiveHoldings(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPWriteNumberIntermediate>> intermediateListMap)
    {
        try
        {
            var registerList = new ArrayList<SimpleInputRegister>();

            var firstOffset = -1;
            for (var entry : intermediateListMap.entrySet())
            {
                var offset = entry.getKey();
                var intermediateList = entry.getValue();

                if (intermediateList.isEmpty())
                {
                    throw new IllegalStateException(
                            "Found an intermediate sub list that is empty while writing holdings");
                }

                if (intermediateList.size() > 1)
                {
                    MainLogger.getInstance()
                            .warning("Trying to write multiple holdings to the same offset " + offset, this);
                }

                if (firstOffset == -1)
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
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp holding registers", exception, this);
            this.disconnect();
        }

    }

    private void readMultipleConsecutiveHoldings(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPReadNumberIntermediate>> intermediateListMap)
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
            for (var intermediateList : intermediateListMap.values())
            {
                var register = response.getRegisters()[x++];
                for (var intermediate : intermediateList)
                {
                    intermediate.setNextWord(register.getValue());
                }
            }
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp holding registers", exception, this);
            this.disconnect();
        }
    }

    private void writeMultipleConsecutiveCoils(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPWriteBitIntermediate>> intermediateListMap)
    {
        try
        {
            var lowestOffset = intermediateListMap.keySet().stream().mapToInt(Integer::intValue).min().orElseThrow();

            var request = new WriteMultipleCoilsRequest(lowestOffset, intermediateListMap.size());
            transaction.setRequest(request);

            var x = 0;
            for (var entry : intermediateListMap.entrySet())
            {
                var intermediateList = entry.getValue();
                if (intermediateList.isEmpty())
                {
                    throw new IllegalStateException(
                            "Found an intermediate sub list that is empty while writing multiple coils");
                }

                if (intermediateList.size() > 1)
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
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while writing Modbus tcp multiple coils", exception, this);
            this.disconnect();
        }
    }

    private void readMultipleCoils(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPReadBitIntermediate>> intermediateListMap)
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
            for (var intermediateList : intermediateListMap.values())
            {
                var coilBit = response.getCoils().getBit(x++);
                for (var intermediate : intermediateList)
                {
                    intermediate.setValue(coilBit);
                }
            }
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp multiple coils", exception, this);
            this.disconnect();
        }
    }

    private void readConsecutiveDiscreteInputs(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPReadBitIntermediate>> intermediateListMap)
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
            for (var intermediateList : intermediateListMap.values())
            {
                var discreteBit = response.getDiscretes().getBit(x++);
                for (var intermediate : intermediateList)
                {
                    intermediate.setValue(discreteBit);
                }
            }
        } catch (Exception exception)
        {
            MainLogger.getInstance().error("Error while reading Modbus tcp discrete inputs", exception, this);
            this.disconnect();
        }
    }

    private void readMultipleConsecutiveInputRegisters(ModbusTransaction transaction,
            Map<Integer, List<ModbusTCPReadNumberIntermediate>> intermediateListMap)
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
            for (var intermediateList : intermediateListMap.values())
            {
                var register = response.getRegisters()[x++];
                for (var intermediate : intermediateList)
                {
                    intermediate.setNextWord(register.getValue());
                }
            }
        } catch (Exception exception)
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
