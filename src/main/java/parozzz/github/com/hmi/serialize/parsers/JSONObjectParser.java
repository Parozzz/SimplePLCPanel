package parozzz.github.com.hmi.serialize.parsers;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;

public abstract class JSONObjectParser<V>
{
    private final String identifier;
    private final Class<V> valueClass;
    public JSONObjectParser(String identifier, Class<V> valueClass)
    {
        this.identifier = identifier;
        this.valueClass = valueClass;
    }

    public Class<V> getValueClass()
    {
        return valueClass;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public boolean hasIdentifier(JSONDataMap jsonDataMap)
    {
        var identifier = jsonDataMap.getString("JSONObjectParser");
        return this.identifier.equals(identifier);
    }

    public void setIdentifier(JSONDataMap jsonDataMap)
    {
        jsonDataMap.set("JSONObjectParser", identifier);
    }

    public JSONDataMap serializeObject(Object object)
    {
        if(valueClass.isInstance(object))
        {
            return serialize(valueClass.cast(object));
        }

        return null;
    }

    public abstract JSONDataMap serialize(V value);
}
