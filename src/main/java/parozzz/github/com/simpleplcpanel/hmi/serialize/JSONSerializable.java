package parozzz.github.com.simpleplcpanel.hmi.serialize;

import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public interface JSONSerializable
{
    JSONDataMap serialize();

    void deserialize(JSONDataMap jsonDataMap);

    default void deserialize(JSONSerializable serializable, JSONDataMap jsonDataMap, String key)
    {
        if(serializable == null)
        {
            return;
        }

        var subJSONDataMap = jsonDataMap.getMap(key);
        if(subJSONDataMap != null)
        {
            serializable.deserialize(subJSONDataMap);
        }
    }
}
