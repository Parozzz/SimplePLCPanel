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
package parozzz.github.com.simpleplcpanel.PLC.siemens.packets.szl;

import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7CommProcessorInfo
{
    
    private final int maxPDULength;
    private final int maxConnections;
    
    protected SiemensS7CommProcessorInfo(SiemensS7SzlData szlData)
    {
        maxPDULength = SiemensS7Util.getShortAt(szlData.dataBuffer, 2);
        maxConnections = SiemensS7Util.getShortAt(szlData.dataBuffer, 4);
    }
    
    public int getMaxPDULength()
    {
        return maxPDULength;
    }
    
    public int getMaxConnections()
    {
        return maxConnections;
    }
    
    public String toString()
    {
        return "CommunicatorProcessor => " +
                "Max PDU Length: " + maxPDULength + ", " +
                "Max Connections: " + maxConnections;
    }
}
