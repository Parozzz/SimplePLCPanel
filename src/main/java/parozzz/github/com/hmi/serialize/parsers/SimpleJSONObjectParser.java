package parozzz.github.com.hmi.serialize.parsers;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;

public abstract class SimpleJSONObjectParser<V> extends JSONObjectParser<V>
{
    public SimpleJSONObjectParser(String identifier, Class<V> valueClass)
    {
        super(identifier, valueClass);
    }

    public abstract void deserialize(V value, JSONDataMap jsonDataMap);
}
