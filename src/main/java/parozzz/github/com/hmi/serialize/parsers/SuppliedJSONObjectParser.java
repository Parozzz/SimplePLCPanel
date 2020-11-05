package parozzz.github.com.hmi.serialize.parsers;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;

public abstract class SuppliedJSONObjectParser<V> extends JSONObjectParser<V>
{
    public SuppliedJSONObjectParser(String identifier, Class<V> valueClass)
    {
        super(identifier, valueClass);
    }

    public abstract V create(JSONDataMap jsonDataMap);
}
