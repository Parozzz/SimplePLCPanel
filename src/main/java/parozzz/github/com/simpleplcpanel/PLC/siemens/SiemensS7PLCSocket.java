package parozzz.github.com.simpleplcpanel.PLC.siemens;

import parozzz.github.com.simpleplcpanel.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public final class SiemensS7PLCSocket
{
    private static final int ISO_TCP_PORT = 102; // ISO_TCP Port
    
    private final SiemensS7Client client;
    private final SocketAddress socketAddress;
    
    private int timeout = 5000;
    
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    
    public SiemensS7PLCSocket(SiemensS7Client client, String ipAddress)
    {
        this.client = client;
        
        socketAddress = new InetSocketAddress(ipAddress, ISO_TCP_PORT);
    }
    
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
    
    public boolean connect()
    {
        socket = new Socket();
        
        try
        {
            socket.connect(socketAddress, timeout);
            socket.setSoTimeout(timeout);
            socket.setTcpNoDelay(true);
            
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            
            return true;
        }
        catch(IOException ex)
        {
            if(client.getDebug())
            {
                ex.printStackTrace();
            }
            
            close();
            return false;
        }
    }
    
    @Nullable
    public InputStream getInputStream()
    {
        return inputStream;
    }
    
    @Nullable
    public OutputStream getOutputStream()
    {
        return outputStream;
    }
    
    public boolean isConnected()
    {
        return socket != null && socket.isConnected();
    }
    
    public void close()
    {
        close(inputStream);
        inputStream = null;
        
        close(outputStream);
        outputStream = null;
        
        close(socket);
        socket = null;
    }
    
    private void close(Closeable closeable)
    {
        if(closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch(IOException exception)
            {
                if(client.getDebug())
                {
                    exception.printStackTrace();
                }
            }
        }
    }
}
