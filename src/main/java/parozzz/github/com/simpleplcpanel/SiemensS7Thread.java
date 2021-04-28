package parozzz.github.com.simpleplcpanel;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.structure.SiemensS7DTLData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.SiemensS7ReadData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.SiemensS7WriteData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata.SiemensS7ReadableWrappedData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata.SiemensS7WritableWrappedData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;

import java.util.Random;

public final class SiemensS7Thread extends Thread
{
    
    private boolean stopped = false;
    
    private SiemensS7Client client;
    
    private SiemensS7Client newClient;
    
    public synchronized void stopThread()
    {
        stopped = true;
    }
    
    public boolean isConnected()
    {
        return client != null && client.isConnected();
    }
    
    public void setConnectionParameters(String ipAddress, int rack, int slot)
    {
        newClient = new SiemensS7Client(ipAddress, rack, slot);
    }
    
    public void run()
    {
        boolean toggleBit = false;
        while(true)
        {
            Thread.onSpinWait();
            
            if(stopped)
            {
                break;
            }
            
            if(newClient != null)
            {
                if(client != null)
                {
                    client.disconnect();
                }
                
                client = newClient;
                newClient = null;
            }
            
            if(client == null)
            {
                continue;
            }
            
            
            try
            {
                if(!client.isConnected())
                {
                    client.connect();
                    continue;
                }
                
                try
                {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
    
                var timestamp = System.currentTimeMillis();
    
                var readBit = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getBit(0), 0);
                var readBit2 = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getBit(1), 0);
                var readBit3 = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getBit(6), 2);
                var readBit4 = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getBit(4), 3);
                var readBit5 = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getBit(2), 7);
    
                var readUnsignedInt = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.WORD, 2);
                var readReal = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.FLOAT, 6);
                var readRandomWord = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.WORD, 7);
                var readSignedInt = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.SHORT, 10);
                var readDouble = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.DOUBLE, 76);
    
    
                var readFirstStringData = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getString(3), 12);
                var readSecondStringData = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.getString(15), 18);
                
                var readData = new SiemensS7ReadData(client, SiemensS7AreaType.DB, 1)
                        .append(readBit)
                        .append(readBit2)
                        .append(readBit3)
                        .append(readBit4)
                        .append(readBit5)
                        .append(readRandomWord)
                        .append(readReal)
                        .append(readUnsignedInt)
                        .append(readSignedInt)
                        .append(readDouble)
                        .append(readFirstStringData)
                        .append(readSecondStringData)
                        .read();
    
                //var stringReadResult = client.readArea(SiemensS7AreaType.DB, 1, 12, 24);
    
                System.out.println("First String: " + readFirstStringData.getValue());
                System.out.println("Second String: " + readSecondStringData.getValue());
    
                System.out.println("Bit: " + readBit.getValue());
                System.out.println("Unsigned Int: " + readUnsignedInt.getValue());
                System.out.println("Real: " + readReal.getValue());
                System.out.println("Signed Int: " + readSignedInt.getValue());
                System.out.println("Double: " + readDouble.getValue());
    
                System.out.println("Bit 4.1: " + client.readBit(SiemensS7AreaType.DB, 1, 4, 1));
                System.out.println("Bit 4.2: " + client.readBit(SiemensS7AreaType.DB, 1, 4, 2));
                System.out.println("Bit 4.3: " + client.readBit(SiemensS7AreaType.DB, 1, 4, 3));
    
                client.writeBit(SiemensS7AreaType.DB, 1, 4, 0, (toggleBit = !toggleBit));
    
                System.out.println("1st Part took: " + (System.currentTimeMillis() - timestamp) + "ms");
                var secondTimestamp = System.currentTimeMillis();
                
                var writeRandomOutData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.BYTE, 0, (byte) new Random().nextInt());
                new SiemensS7WriteData(client, SiemensS7AreaType.OUTPUT).append(writeRandomOutData).write();
                
                var writeStringData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.getString(30), 36, "FIGA NEH 1234 4567");
                var writeIntData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.WORD, 68, 3586);
                var writeFloatData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.FLOAT, 70, (float) -5.67);
                var writeSignedIntData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.SHORT, 74, (short) -684);
                var writeDoubleData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.DOUBLE, 84, 56458.55667);

                new SiemensS7WriteData(client, SiemensS7AreaType.DB, 1)
                        .append(writeStringData)
                        .append(writeIntData)
                        .append(writeFloatData)
                        .append(writeSignedIntData)
                        .append(writeDoubleData)
                        .write();
    
                var testDTLData = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.DTL, 0);
                var testTimerData = new SiemensS7ReadableWrappedData<>(SiemensS7DataStorage.TIMER, 24);
    
                readData = new SiemensS7ReadData(client, SiemensS7AreaType.DB, 2)
                        .append(testDTLData)
                        .append(testTimerData)
                        .read();
    
                System.out.println("Test DTL: " + testDTLData.getValue());
                System.out.println("Test Timer: " + testTimerData.getValue());
    
                var writeDTLData = new SiemensS7WritableWrappedData<>(SiemensS7DataStorage.DTL, 12, SiemensS7DTLData.DTL.ofCurrentCalendar());
                new SiemensS7WriteData(client, SiemensS7AreaType.DB, 2).append(writeDTLData).write();
    
                System.out.println("2th Part took: " + (System.currentTimeMillis() - secondTimestamp) + " ms");
    
                System.out.println("PLC Model: " + client.getModelInfo().getModel());
                System.out.println("PLC Status: " + client.getPLCStatus().name());
                System.out.println(client.getCommProcessorInfo().toString());
    
                //This whole
    
                System.out.println("Total Time Taken: " + (System.currentTimeMillis() - timestamp) + "ms");
    
                System.out.println("====================");
            }
            catch(SiemensS7Error.SiemensS7Exception exception)
            {
                if(client.getDebug())
                {
                    exception.printStackTrace();
                }
            }
        }
    }
    
}
