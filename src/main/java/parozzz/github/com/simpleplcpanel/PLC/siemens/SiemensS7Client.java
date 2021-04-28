/*=============================================================================|
|  PROJECT Moka7                                                         1.0.2 |
|==============================================================================|
|  Copyright (C) 2013, 2016 Davide Nardella                                    |
|  All rights reserved.                                                        |
|==============================================================================|
|  SNAP7 is free software: you can redistribute it and/or modify               |
|  it under the terms of the Lesser GNU General Public License as published by |
|  the Free Software Foundation, either version 3 of the License, or under     |
|  EPL Eclipse Public License 1.0.                                             |
|                                                                              |
|  This means that you have to chose in advance which take before you import   |
|  the library into your project.                                              |
|                                                                              |
|  SNAP7 is distributed in the hope that it will be useful,                    |
|  but WITHOUT ANY WARRANTY; without even the implied warranty of              |
|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE whatever license you    |
|  decide to adopt.                                                            |
|                                                                              |
|=============================================================================*/
package parozzz.github.com.simpleplcpanel.PLC.siemens;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7ConnectionPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7DatePacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7GetPLCStatusPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.SiemensS7NegotiatePDULengthPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.read.SiemensS7ReadBitPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.read.SiemensS7ReadDataPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.write.SiemensS7WriteBitPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.readwrite.write.SiemensS7WriteDataPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl.SiemensS7CommProcessorInfo;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl.SiemensS7ModelInfo;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl.SiemensS7SzlData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl.SiemensS7SzlPacket;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Status;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

import java.io.IOException;
import java.util.Date;


public class SiemensS7Client
{
    private static final int MinPduSize = 16;
    private static final int DefaultPduSizeRequested = 480;
    private static final int IsoHeaderSize = 7; // TPKT+COTP Header Size
    private static final int MaxPduSize = DefaultPduSizeRequested + IsoHeaderSize;

    private final SiemensS7PLCSocket plcSocket;

    private final String ipAddress;
    private final int rack;
    private final int slot;
    
    private final SiemensS7ReadDataPacket readDataPacket;
    private final SiemensS7ReadBitPacket readBitPacket;
    private final SiemensS7WriteDataPacket writeDataPacket;
    private final SiemensS7WriteBitPacket writeBitPacket;
    private final SiemensS7NegotiatePDULengthPacket negotiatePDULengthPacket;
    private final SiemensS7ConnectionPacket connectionPacket;
    private final SiemensS7GetPLCStatusPacket getPLCStatusPacket;
    private final SiemensS7DatePacket siemensDatePacket;
    private final SiemensS7SzlPacket szlPacket;
    
    //PDU = Protocol Data Unit
    public final byte[] PDU = new byte[2048];
    private int pduLength = 0;
    
    //public int lastError = 0;
    private int receiveTimeout = 2000;
    
    private SiemensS7Util.ConnectionType connectionType = SiemensS7Util.ConnectionType.PG;

    private boolean connected = false;

    private boolean debug = false;
    
    public SiemensS7Client(String ipAddress, int rack, int slot)
    {
        plcSocket = new SiemensS7PLCSocket(this, ipAddress);

        this.ipAddress = ipAddress;
        this.rack = rack;
        this.slot = slot;

        this.readDataPacket = new SiemensS7ReadDataPacket(this, plcSocket);
        this.readBitPacket = new SiemensS7ReadBitPacket(this, plcSocket);
        this.writeDataPacket = new SiemensS7WriteDataPacket(this, plcSocket);
        this.writeBitPacket = new SiemensS7WriteBitPacket(this, plcSocket);
        this.negotiatePDULengthPacket = new SiemensS7NegotiatePDULengthPacket(this, plcSocket);
        this.connectionPacket = new SiemensS7ConnectionPacket(this, plcSocket);
        this.getPLCStatusPacket = new SiemensS7GetPLCStatusPacket(this, plcSocket);
        this.siemensDatePacket = new SiemensS7DatePacket(this, plcSocket);
        this.szlPacket = new SiemensS7SzlPacket(this, plcSocket);
    }


    public int getRack()
    {
        return rack;
    }

    public int getSlot()
    {
        return slot;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public boolean getDebug()
    {
        return debug;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        plcSocket.setTimeout(connectionTimeout);
    }

    public void setReceiveTimeout(int receiveTimeout)
    {
        this.receiveTimeout = receiveTimeout;
    }
    
    public void setConnectionType(SiemensS7Util.ConnectionType connectionType)
    {
        this.connectionType = connectionType;
    }
    
    public int getPDULength()
    {
        return pduLength;
    }
    
    public SiemensS7ISOReceivedData receiveIsoPacket() throws SiemensS7Error.SiemensS7Exception
    {
        while(true)
        {
            //Read the TPKT Header (4 bytes)
            receivePacket(PDU, 0, 4);
            
            var tpktPduLength = SiemensS7Util.getWordAt(PDU, 2);
            if(tpktPduLength == IsoHeaderSize) //If the packet lenght is the same as only the header, it has not data inside. Error?
            {
                receivePacket(PDU, 4, 3); //Read remaining 3 bytes from the head to clear the buffer
            }
            else
            {
                if((tpktPduLength > MaxPduSize) || (tpktPduLength < MinPduSize))
                {
                    SiemensS7Error.ISOInvalidPDU.throwException(this);
                }
                else
                {
                    // exit the loop correctly if a valid length is received != 7 && >16 && <247
                    receivePacket(PDU, 4, 3); //Read remaining 3 bytes from COTP header
                    receivePacket(PDU, 7, tpktPduLength - IsoHeaderSize); // Receives the S7 Payload
                    return new SiemensS7ISOReceivedData(tpktPduLength, PDU[5] == (byte) 0xD0); //Byte 6 seems like the Connection OK byte
                }
            }
        }
    }
    
    private void receivePacket(byte[] buffer, int bufferOffset, int size) throws SiemensS7Error.SiemensS7Exception
    {
        if(!plcSocket.isConnected())
        {
            SiemensS7Error.TCPNotConnected.throwException(this);
        }
        
        var inputStream = plcSocket.getInputStream();
        try
        {
            var timestamp = System.currentTimeMillis();
            while(inputStream.available() < size)
            {
                if(System.currentTimeMillis() - timestamp > receiveTimeout)
                {
                    inputStream.read(PDU, 0, inputStream.available()); // If timeout we clean the buffer
                    SiemensS7Error.TCPDataReceiveTimeout.throwException(this);
                }
            }
            
            var bytesRead = inputStream.read(buffer, bufferOffset, size);
            if(bytesRead == 0)
            {
                SiemensS7Error.TCPConnectionReset.throwException(this, "Received 0 bytes.");
            }
        }
        catch(IOException ex)
        {
            SiemensS7Error.TCPDataReceiveTimeout.throwException(this);
        }
    }
    
    public void connect() throws SiemensS7Error.SiemensS7Exception
    {
        if(plcSocket.isConnected())
        {
            SiemensS7Error.TCPAlreadyConnected.throwException(this);
        }
        
        //In case the connection is not completely established (TCP connection + ISO connection + PDU negotiation)
        //we close the socket and its IO streams to revert the object back to pre-Connect() state
        //All the exceptions inside here WILL close the connection
        if(plcSocket.connect()) // First stage : TCP Connection
        {
            connectionPacket.setConnectionType(connectionType) // Second stage : ISOTCP (ISO 8073) Connection
                    .setPLCPosition(rack, slot)
                    .connect();
            
            pduLength = negotiatePDULengthPacket.exchangePDULength();
            connected = true;
        }
        else
        {
            SiemensS7Error.TCPConnectionFailed.throwException(this);
        }
    }
    
    public void disconnect()
    {
        connected = false;

        pduLength = 0;
        plcSocket.close();
    }
    
    public boolean readBit(SiemensS7AreaType areaType, int byteNumber, int bitNumber) throws SiemensS7Error.SiemensS7Exception
    {
        return readBit(areaType, 0, byteNumber, bitNumber);
    }
    
    public boolean readBit(SiemensS7AreaType areaType, int dbNumber, int byteNumber, int bitNumber) throws SiemensS7Error.SiemensS7Exception
    {
        var index = (byteNumber * 8) + bitNumber;
        return readBitPacket.copyPacket()
                .setAreaType(areaType, dbNumber)
                .setMemoryOffset(index)
                .sendPacket()
                .receiveResponse();
    }
    
    public SiemensS7ReadResult readArea(SiemensS7AreaType areaType, int startIndex, int dataAmount) throws SiemensS7Error.SiemensS7Exception
    {
        return readArea(areaType, 0, startIndex, dataAmount);
    }
    
    public SiemensS7ReadResult readArea(SiemensS7AreaType areaType, int dbNumber, int startIndex, int dataAmount) throws SiemensS7Error.SiemensS7Exception
    {
        var dataBuffer = new byte[dataAmount + 4]; //+4 just to be safe
        
        var maxElementsAmount = (pduLength - 18); // 18 = Reply read telegram header
        var totElements = dataAmount;
        
        var index = startIndex;
        while(totElements > 0) //The while is in case a dataAmount higher than the maxLength (PDULenght) is requested
        {
            var numElements = Math.min(maxElementsAmount, totElements);
            
            //If the data request is too big ~500bytes, the request get splitted in multiple parts
            //It adds those with an offset to the dataBuffer and it start from zero.
            //The memory address index is increased each loop by the number of elements and this value
            //minus the start memory address index is my offset.
            var bufferOffset = index - startIndex;
            readDataPacket.copyPacket()
                    .setNumberOfElements(numElements)
                    .setAreaType(areaType, dbNumber)
                    .setMemoryOffset(index)
                    .sendPacket()
                    .receiveResponse(bufferOffset, dataBuffer);
            
            totElements -= numElements;
            index += numElements;
        }
        
        return new SiemensS7ReadResult(dataBuffer);
    }
    
    public void writeBit(SiemensS7AreaType areaType, int byteNumber, int bitNumber, boolean bit) throws SiemensS7Error.SiemensS7Exception
    {
        writeBit(areaType, 0, byteNumber, bitNumber, bit);
    }
    
    public void writeBit(SiemensS7AreaType areaType, int dbNumber, int byteNumber, int bitNumber, boolean bit) throws SiemensS7Error.SiemensS7Exception
    {
        var index = (byteNumber * 8) + bitNumber;
        writeBitPacket.copyPacket()
                .setMemoryOffset(index)
                .setAreaType(areaType, dbNumber)
                .setBit(bit)
                .sendPacket()
                .receiveResponse();
    }
    
    public void writeArea(SiemensS7AreaType areaType, int startIndex, int dataAmount, byte[] dataBuffer) throws SiemensS7Error.SiemensS7Exception
    {
        writeArea(areaType, 0, startIndex, dataAmount, dataBuffer);
    }
    
    public void writeArea(SiemensS7AreaType areaType, int dbNumber, int startIndex, int dataAmount, byte[] dataBuffer) throws SiemensS7Error.SiemensS7Exception
    {
        var maxElementsCount = (pduLength - 35); // 35 = Write Telegram Header
        var totElements = dataAmount;
        
        var index = startIndex;
        while(totElements > 0)
        {
            var numElements = Math.min(maxElementsCount, totElements);
            
            var offset = index - startIndex;
            writeDataPacket.copyPacket()
                    .setNumberOfElements(numElements)
                    .setAreaType(areaType, dbNumber)
                    .setMemoryOffset(index)
                    .copyData(dataBuffer, offset)
                    .sendPacket()
                    .receiveResponse();
            
            totElements -= numElements;
            index += numElements;
        }
    }
    
    public SiemensS7SzlData readSZL(int id, int index) throws SiemensS7Error.SiemensS7Exception
    {
        return szlPacket.readSZL(id, index);
    }
    
    public SiemensS7CommProcessorInfo getCommProcessorInfo() throws SiemensS7Error.SiemensS7Exception
    {
        return szlPacket.getCommProcessorInfo();
    }
    
    public SiemensS7ModelInfo getModelInfo() throws SiemensS7Error.SiemensS7Exception
    {
        return szlPacket.getModelInfo();
    }
    
    @Nullable
    public Date getPlcDateTime() throws SiemensS7Error.SiemensS7Exception
    {
        return siemensDatePacket.getPlcDateTime();
    }
    
    public void setPLCSystemDateTime() throws SiemensS7Error.SiemensS7Exception
    {
        setPLCDateTime(new Date());
    }
    
    public void setPLCDateTime(Date dateTime) throws SiemensS7Error.SiemensS7Exception
    {
        siemensDatePacket.setPLCDateTime(dateTime);
    }
    
    @Nullable
    public SiemensS7Status getPLCStatus() throws SiemensS7Error.SiemensS7Exception
    {
        return getPLCStatusPacket.getStatus();
    }
    
}