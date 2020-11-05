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
package parozzz.github.com.PLC.siemens.packets.szl;

import parozzz.github.com.PLC.siemens.util.SiemensS7Util;

public final class SiemensS7ModelInfo
{
    private final String model;
    private final int V1;
    private final int V2;
    private final int V3;
    
    protected SiemensS7ModelInfo(SiemensS7SzlData szlData)
    {
        var buffer = szlData.dataBuffer;
        var dataSize = szlData.dataSize;
        
        model = SiemensS7Util.getStringWithFixedLength(buffer, 2, 20);
        V1 = buffer[dataSize - 3];
        V2 = buffer[dataSize - 2];
        V3 = buffer[dataSize - 1];
    }

    public String getModel()
    {
        return model;
    }
    
    public int getV1()
    {
        return V1;
    }
    
    public int getV2()
    {
        return V2;
    }
    
    public int getV3()
    {
        return V3;
    }
}
