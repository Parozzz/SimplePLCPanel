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
package parozzz.github.com.simpleplcpanel.PLC.siemens.util;

import java.util.Calendar;
import java.util.Date;

// Step 7 Constants and Conversion helper class
public class SiemensS7Util
{
    public enum ConnectionType
    {
        PG((byte) 0x01),
        OP((byte) 0x02),
        S7_BASIC((byte) 0x03);
        
        private final byte id;
        
        ConnectionType(byte id)
        {
            this.id = id;
        }
        
        public byte getId()
        {
            return id;
        }
    }
    
    public static int limitBit(int bit)
    {
        return Math.min(7, Math.max(0, bit));
    }
    
    // Returns the bit at Pos.Bit 
    public static boolean getBitAt(byte[] buffer, int index, int bit)
    {
        bit = limitBit(bit);
        return (buffer[index] & (1 << bit)) != 0;
    }
    
    public static int getWordAt(byte[] buffer, int index)
    {
        //Seems like java when converting a signed byte value to an integer
        //likes to do a compliment of 2 to get stuff done
        var highByte = buffer[index] & 0xFF;
        var lowByte = buffer[index + 1] & 0xFF;
        return (highByte << 8) + lowByte;
    }
    
    // Returns a 16 bit signed value : from -32768 to 32767
    public static short getShortAt(byte[] buffer, int index)
    {
        var highByte = buffer[index];
        var lowByte = buffer[index + 1] & 0xFF;
        return (short) ((highByte << 8) + lowByte);
    }
    
    // Returns a 32 bit unsigned value : from 0 to 4294967295 (2^32-1)
    public static long getDWordAt(byte[] buffer, int index)
    {
        long result = buffer[index] & 0xFF; //Will remove the sign
        for(int x = 1; x < 4; x++)
        {
            result <<= 8;
            result |= (buffer[index + x] & 0xFF);
        }
        
        return result;
    }
    
    // Returns a 32 bit signed value : from 0 to 4294967295 (2^32-1)
    public static int getDIntAt(byte[] buffer, int index)
    {
        int result = buffer[index];
        for(int x = 1; x < 4; x++)
        {
            result <<= 8;
            result |= (buffer[index + x] & 0xFF);
        }
        return result;
    }
    
    // Returns a 32 bit floating point
    public static float getFloatAt(byte[] buffer, int index)
    {
        return Float.intBitsToFloat(getDIntAt(buffer, index));
    }
    
    public static double getDoubleAt(byte[] buffer, int offset)
    {
        long longValue = buffer[offset];
        for(int x = 1; x < 8; x++)
        {
            longValue <<= 8;
            longValue |= (buffer[offset + x] & 0xFF);
        }
        return Double.longBitsToDouble(longValue);
    }
    
    
    //Seems like how siemens allocate string is:
    //First Byte: Number of Byte the whole string is made of
    //Second Byte: Number of character populated
    //N0 , N1 ... N(Second Byte - 1)
    //Plus seems like string are always allocated in group of 2 bytes.
    //So if the total length is 5 will be effectively 6
    
    // Returns an ASCII string
    public static String getStringAt(byte[] buffer, int index)
    {
        int memoryStringLength = buffer[index];
        int stringLength = buffer[index + 1];
        
        var stringBuilder = new StringBuilder();
        for(int x = 0; x < stringLength; x++)
        {
            //This is to avoid wrong configuration and it will truncate the string in the worst case
            var bufferIndex = x + index + 2;
            if(bufferIndex == buffer.length)
            {
                break;
            }

            stringBuilder.append((char) buffer[bufferIndex]);
        }
        return stringBuilder.toString();
    }

    public static String getStringWithFixedLength(byte[] buffer, int index, int length)
    {
        var stringBuilder = new StringBuilder();
        for(int x = index; x < (index + length); x++)
        {
            stringBuilder.append((char) buffer[x]);
        }
        return stringBuilder.toString();
    }
    
    public static void setStringAt(byte[] buffer, int plcStringLength, int index, String value)
    {
        if(value.length() >= 254)
        {
            return;
        }

        //Do not base on the length of the value, get the min between what is passed and the value
        //In the worst case the value is truncated
        var validStringLength = Math.min(plcStringLength, value.length());

        buffer[index] = (byte) plcStringLength;
        buffer[index + 1] = (byte) validStringLength;

        for(int x = 0; x < validStringLength; x++)
        {
            buffer[index + 2 + x] = (byte) value.charAt(x);
        }
    }
    
    public static Date getDateAt(byte[] buffer, int pos)
    {
        var calendar = Calendar.getInstance();
        
        var year = SiemensS7Util.convertBCDToByte(buffer[pos]);
        year += year < 90 ? 2000 : 1900;
        calendar.set(year,
                SiemensS7Util.convertBCDToByte(buffer[pos + 1]) - 1,
                SiemensS7Util.convertBCDToByte(buffer[pos + 2]),
                SiemensS7Util.convertBCDToByte(buffer[pos + 3]),
                SiemensS7Util.convertBCDToByte(buffer[pos + 4]),
                SiemensS7Util.convertBCDToByte(buffer[pos + 5]));
        
        return calendar.getTime();
    }
    
    public static void setBitAt(byte[] buffer, int pos, int bit, boolean value)
    {
        bit = limitBit(bit);
        
        if(value)
        {
            buffer[pos] |= (1 << bit);
        }
        else
        {
            buffer[pos] &= ~(1 << bit);
        }
    }
    
    public static void setWordAt(byte[] buffer, int index, int value)
    {
        int word = value & 0xFFFF;
        buffer[index] = (byte) (word >> 8);
        buffer[index + 1] = (byte) word;
    }
    
    public static void setShortAt(byte[] buffer, int index, short value)
    {
        buffer[index] = (byte) (value >> 8);
        buffer[index + 1] = (byte) value;
    }
    
    public static void setDWordAt(byte[] buffer, int index, long value)
    {
        //Remove the sign
        var word = Integer.toUnsignedLong((int) value);
        buffer[index + 3] = (byte) (word & 0xFF);
        buffer[index + 2] = (byte) ((word >> 8) & 0xFF);
        buffer[index + 1] = (byte) ((word >> 16) & 0xFF);
        buffer[index] = (byte) ((word >> 24) & 0xFF);
    }
    
    public static void setDIntAt(byte[] buffer, int index, int value)
    {
        buffer[index + 3] = (byte) (value & 0xFF);
        buffer[index + 2] = (byte) ((value >> 8) & 0xFF);
        buffer[index + 1] = (byte) ((value >> 16) & 0xFF);
        buffer[index] = (byte) ((value >> 24) & 0xFF);
    }
    
    public static void setFloatAt(byte[] buffer, int index, float value)
    {
        setDIntAt(buffer, index, Float.floatToIntBits(value));
    }
    
    public static void setDoubleAt(byte[] buffer, int offset, double value)
    {
        var longValue = Double.doubleToLongBits(value);
        
        for(int x = 0; x < 8; x++)
        {
            var shiftedValue = (longValue >> (8 * x)) & 0xFF;
            buffer[offset + (7 - x)] = (byte) shiftedValue;
        }
    }
    
    public static void setDateAt(byte[] buffer, int pos, Date date)
    {
        var calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        var year = calendar.get(Calendar.YEAR);
        year -= year > 1999 ? 2000 : 0;
        
        buffer[pos] = convertByteToBCD(year);
        buffer[pos + 1] = convertByteToBCD(calendar.get(Calendar.MONTH) + 1);
        buffer[pos + 2] = convertByteToBCD(calendar.get(Calendar.DAY_OF_MONTH));
        buffer[pos + 3] = convertByteToBCD(calendar.get(Calendar.HOUR_OF_DAY));
        buffer[pos + 4] = convertByteToBCD(calendar.get(Calendar.MINUTE));
        buffer[pos + 5] = convertByteToBCD(calendar.get(Calendar.SECOND));
        buffer[pos + 6] = 0;
        buffer[pos + 7] = convertByteToBCD(calendar.get(Calendar.DAY_OF_WEEK));
    }
    
    private static int convertBCDToByte(byte B)
    {
        return ((B >> 4) * 10) + (B & 0x0F);
    }
    
    private static byte convertByteToBCD(int Value)
    {
        return (byte) (((Value / 10) << 4) | (Value % 10));
    }
    
}
