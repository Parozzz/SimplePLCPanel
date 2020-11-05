package parozzz.github.com.PLC.siemens.rwdata;

import parozzz.github.com.PLC.siemens.SiemensS7Client;
import parozzz.github.com.PLC.siemens.rwdata.wrappeddata.SiemensS7WritableWrappedData;
import parozzz.github.com.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.PLC.siemens.util.SiemensS7Error;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SiemensS7WriteData
{
    private final SiemensS7Client client;
    private final SiemensS7AreaType areaType;
    private final int dbNumber;
    private final List<SiemensS7WritableWrappedData<?, ?>> wrappedDataList;
    
    public SiemensS7WriteData(SiemensS7Client client, SiemensS7AreaType areaType)
    {
        this(client, areaType, 0);
    }
    
    public SiemensS7WriteData(SiemensS7Client client, SiemensS7AreaType areaType, int dbNumber)
    {
        this.client = client;
        this.areaType = areaType;
        this.dbNumber = dbNumber;
        
        wrappedDataList = new ArrayList<>();
    }
    
    public SiemensS7WriteData append(SiemensS7WritableWrappedData<?, ?> data)
    {
        wrappedDataList.add(data);
        return this;
    }
    
    public void write() throws SiemensS7Error.SiemensS7Exception
    {
        wrappedDataList.sort(Comparator.comparing(SiemensS7WritableWrappedData::getOffset));
        
        var groupWrappedDataList = new ArrayList<SiemensS7WritableWrappedData<?, ?>>();
    
        SiemensS7WritableWrappedData<?, ?> oldWrappedData = null;
        for(var wrappedData : wrappedDataList)
        {
            var dataOffset = wrappedData.getOffset();
            
            if(oldWrappedData != null)
            {
                var oldDataOffset = oldWrappedData.getOffset();
                var oldDataSize = oldWrappedData.getByteSize();
                
                var calculatedOffset = oldDataOffset + oldDataSize;
                //If the calculated new offset is higher than the data offset
                if(calculatedOffset > dataOffset)
                {
                    SiemensS7Error.OverlappingOffsetWritingData.throwException(client);
                }
                
                //If the old index + old size is equals to the new index means they are consecutive
                if(calculatedOffset != dataOffset)
                {
                    this.parseWrappedDataList(groupWrappedDataList);
                    groupWrappedDataList.clear();
                }
            }
            
            //In case offset + size does not match (Not consecutive data) the list is cleared.
            groupWrappedDataList.add(wrappedData);
            oldWrappedData = wrappedData;
        }
        
        //Parse any remaining data
        this.parseWrappedDataList(groupWrappedDataList);
        
    }
    
    private void parseWrappedDataList(List<SiemensS7WritableWrappedData<?, ?>> wrappedDataList) throws SiemensS7Error.SiemensS7Exception
    {
        if(wrappedDataList.isEmpty())
        {
            return;
        }
        
        var firstOffset = -1;
    
        var groupSize = 0;
        for(var wrappedData : wrappedDataList)
        {
            if(firstOffset == -1)
            {
                firstOffset = wrappedData.getOffset();
            }
        
            groupSize += wrappedData.getByteSize();
        }
    
        var buffer = new byte[groupSize];
    
        int bufferOffset = 0;
        for(var wrappedData : wrappedDataList)
        {
            wrappedData.writeBuffer(buffer, bufferOffset);
            bufferOffset += wrappedData.getByteSize();
        }
    
        client.writeArea(areaType, dbNumber, firstOffset, buffer.length, buffer);
    }
}
