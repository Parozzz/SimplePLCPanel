package parozzz.github.com.simpleplcpanel.hmi.serialize.property;

import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public abstract class SerializableProperty<T>
{
    protected final String key;
    public SerializableProperty(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public abstract void set(JSONDataMap jsonDataMap);

    public abstract void load(JSONDataMap jsonDataMap);
}
