package parozzz.github.com.simpleplcpanel.hmi.serialize.parsers;

import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;

public abstract class SuppliedJSONObjectParser<V> extends JSONObjectParser<V>
{
    public SuppliedJSONObjectParser(String identifier, Class<V> valueClass)
    {
        super(identifier, valueClass);
    }

    public abstract V create(JSONDataMap jsonDataMap);
}
