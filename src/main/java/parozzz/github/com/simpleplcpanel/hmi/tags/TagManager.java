package parozzz.github.com.simpleplcpanel.hmi.tags;

import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;

import java.util.HashMap;
import java.util.Map;

public final class TagManager
{
    private final CommunicationDataHolder communicationDataHolder;
    private final Map<String, Tag> tagMap;

    public TagManager(CommunicationDataHolder communicationDataHolder)
    {
        this.communicationDataHolder = communicationDataHolder;
        this.tagMap = new HashMap<>();
    }

}
