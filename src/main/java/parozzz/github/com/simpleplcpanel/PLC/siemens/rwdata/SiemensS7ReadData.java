package parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata;

import parozzz.github.com.simpleplcpanel.PLC.siemens.SiemensS7Client;
import parozzz.github.com.simpleplcpanel.PLC.siemens.rwdata.wrappeddata.SiemensS7ReadableWrappedData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7Error;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SiemensS7ReadData
{
    private final SiemensS7Client client;
    private final SiemensS7AreaType areaType;
    private final int dbNumber;
    private final List<SiemensS7ReadableWrappedData<?, ?>> wrappedDataList;
    
    public SiemensS7ReadData(SiemensS7Client client, SiemensS7AreaType areaType)
    {
        this(client, areaType, 0);
    }
    
    public SiemensS7ReadData(SiemensS7Client client, SiemensS7AreaType areaType, int dbNumber)
    {
        this.client = client;
        this.areaType = areaType;
        this.dbNumber = dbNumber;
    
        wrappedDataList = new ArrayList<>();
    }
    
    public SiemensS7ReadData append(SiemensS7ReadableWrappedData<?, ?> data)
    {
        wrappedDataList.add(data);
        return this;
    }
    
    public SiemensS7ReadData read() throws SiemensS7Error.SiemensS7Exception
    {
        if(wrappedDataList.size() == 1)
        {
            this.parseWrappedDataList(wrappedDataList);
            return this;
        }
    
        wrappedDataList.sort(Comparator.comparing(SiemensS7ReadableWrappedData::getOffset));
        
        var groupWrappedDataList = new ArrayList<SiemensS7ReadableWrappedData<?, ?>>();
    
        SiemensS7ReadableWrappedData<?, ?> oldWrappedData = null;
        
        //DONE LIKE THIS IS INVALID. IF A VALUE WANT TO READ DATA THAT IS A PART OF ANOTHER (LIKE READING A SINGLE WORD INSIDE A DOUBLE WORD)
        //THIS WILL BREAK! NEED TO USE OFFSETS AND USE DIFFERENTIALS OFFSETS BASED ON THE LOWER
        
        int highestNextOffset = 0;
        for(var wrappedData : wrappedDataList)
        {
            var dataOffset = wrappedData.getOffset();
            var dataByteSize = wrappedData.getByteSize();
            
            if(oldWrappedData == null)
            {
                highestNextOffset = dataOffset;
            }
            else
            {
                //This system is based on the fact that there could be data that read a part inside another data
                //DWord at Offset 4
                //Bit at Offset 1
                
                //If the highest next offset is lower than the actual, it means there is a hole
                //between two offsets and the data request should be splitted.
                //It should always be lower or equal to stay inside the same group.
                if(highestNextOffset < dataOffset)
                {
                    this.parseWrappedDataList(groupWrappedDataList);
                    groupWrappedDataList.clear();
                }
            
                
                var nextOffset = dataOffset + dataByteSize;
                if(nextOffset > highestNextOffset)
                {
                    highestNextOffset = nextOffset;
                }
            }
    
            //Add it at the end, in case the check for sizes and offset before is true,
            //data is sent and list gets cleared
            groupWrappedDataList.add(wrappedData);
            
            oldWrappedData = wrappedData;
        }
        
        //Parse remaining wrapped data
        this.parseWrappedDataList(groupWrappedDataList);

        return this;
    }
    
    private void parseWrappedDataList(List<SiemensS7ReadableWrappedData<?, ?>> wrappedDataList) throws SiemensS7Error.SiemensS7Exception
    {
        if(wrappedDataList.isEmpty())
        {
            return;
        }
        
        int firstOffset = -1;
        
        int groupTotalSize = 0;
        for(var wrappedData : wrappedDataList)
        {
            if(firstOffset == -1)
            {
                firstOffset = wrappedData.getOffset();
            }
            
            groupTotalSize += wrappedData.getByteSize();
        }
        
        var readResult = client.readArea(areaType, dbNumber, firstOffset, groupTotalSize);
        for(var wrappedData : wrappedDataList)
        {
            //Since this is ordered in creascent mode, the first offset is always the lower
            var bufferOffset = wrappedData.getOffset() - firstOffset;
            wrappedData.readBuffer(readResult.getBuffer(), bufferOffset);
        }
    }
}
