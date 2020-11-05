package parozzz.github.com;

import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ModbusTestThread extends Thread
{
    private final TCPMasterConnection masterConnection;
    public ModbusTestThread() throws UnknownHostException
    {
        this.masterConnection = new TCPMasterConnection(InetAddress.getByName("192.168.1.5")); //the connection
    }


    @Override
    public void run()
    {
        while(true)
        {
            if(!masterConnection.isConnected())
            {
                try
                {
                    masterConnection.connect();
                    System.out.println("Connected to PLC");
                } catch (Exception exception)
                {
                    exception.printStackTrace();
                }
            }

            if(!masterConnection.isConnected())
            {
                System.out.println("Not connected");
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException)
                {
                    interruptedException.printStackTrace();
                }
                continue;
            }

            var transaction = new ModbusTCPTransaction(); //the transaction

            var req = new ReadMultipleRegistersRequest(21000, 1); //ref -> offset to start reading from, count -> word amount
            transaction.setConnection(masterConnection);
            transaction.setRequest(req);

            try
            {
                transaction.execute();

                var response = (ReadMultipleRegistersResponse) transaction.getResponse();

                int x = 0;
                for(var register : response.getRegisters())
                {
                    System.out.print(x++ + " = " + register.getValue() + ", ");
                }
                System.out.println();
            } catch (ModbusException e)
            {
                e.printStackTrace();
            }

            try
            {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();
            }
        }
    }
            /*
        var transaction = new ModbusTCPTransaction(); //the transaction

        var req = new ReadInputRegistersRequest(0, 10); //ref -> offset to start reading from, count -> word amount
        transaction.setRequest(req);

        try
        {
            transaction.execute();

            var response = (ReadInputRegistersResponse) transaction.getResponse();
        } catch (ModbusException e)
        {
            e.printStackTrace();
        }*/
}
