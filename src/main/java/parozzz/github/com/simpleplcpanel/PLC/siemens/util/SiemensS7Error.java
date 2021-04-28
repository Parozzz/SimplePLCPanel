package parozzz.github.com.simpleplcpanel.PLC.siemens.util;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;

public enum SiemensS7Error
{
    
    TCPNotConnected("TCP Not Connected"),
    TCPAlreadyConnected("TCP is already connected"),
    TCPConnectionFailed("TCP Connection failed.", true),
    TCPDataSend("TCP Sending error.", true),
    TCPDataReceive("TCP Receiving error.", true),
    TCPDataReceiveTimeout("Data Receiving timeout.", true),
    TCPConnectionReset("Connection reset by the peer.", true),
    ISOInvalidPDU("Invalid ISO PDU received.", true),
    ISOConnectionFailed("ISO connection refused by the CPU.", true),
    ISONegotiationPDU("ISO error negotiating the PDU length.", true),
    //This might imply that the data amount that is trying to read exceed the one inside the PLC memory
    InvalidDataRead("S7 Error reading data from the CPU."),
    //This might imply that the data amount that is trying to write exceed the one inside the PLC memory
    InvalidDataWrite("S7 Error writing data to the CPU."),
    BufferTooSmall("The Buffer supplied to the function is too small."),
    FunctionError("S7 function refused by the CPU."),
    InvalidParameters("Invalid parameters supplied to the function."),
    OverlappingOffsetWritingData("Two or more offsets overlap inside the same write data");
    
    private final String description;
    private final boolean disconnect;
    SiemensS7Error(String description)
    {
        this(description, false);
    }
    
    SiemensS7Error(String description, boolean disconnect)
    {
        this.description = description;
        this.disconnect = disconnect;
    }
    
    public String getDescription()
    {
        return description;
    }

    public void throwException(SiemensS7Client client) throws SiemensS7Exception
    {
        this.throwException(client, "");
    }

    public void throwException(SiemensS7Client client, String extraInformation) throws SiemensS7Exception
    {
        if(disconnect)
        {
            client.disconnect();
        }

        String description = this.description;
        if(extraInformation != null && !extraInformation.isEmpty())
        {
            description += extraInformation;
        }
        throw new SiemensS7Exception(description, this);
    }
    
    public static class SiemensS7Exception extends Exception
    {
        private final SiemensS7Error error;
        public SiemensS7Exception(String description, SiemensS7Error error)
        {
            super(description);

            this.error = error;
        }

        public SiemensS7Error getError()
        {
            return error;
        }
    }
}
