package parozzz.github.com.simpleplcpanel.hmi.serialize.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SimpleJSONObjectParser;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class JSONDataMap extends JSONData<JSONObject>
{
    public JSONDataMap()
    {
        super(new JSONObject());
    }

    public JSONDataMap(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public void set(String key, Object object)
    {
        if (object == null)
        {
            return;
        }

        json.put(key, super.parseSetObject(object));
    }

    public <T, N> void set(String key, T object, Function<T, N> function)
    {
        if (object == null)
        {
            return;
        }

        var setObject = function.apply(object);
        this.set(key, setObject);
    }

    public Object get(String key)
    {
        return json.get(key);
    }

    public JSONDataMap getMap(String key)
    {
        var jsonObject = get(key, JSONObject.class);
        return jsonObject == null
                ? null
                : new JSONDataMap(jsonObject);
    }

    public JSONDataArray getArray(String key)
    {
        var jsonArray = get(key, JSONArray.class);
        return jsonArray == null
                ? null
                : new JSONDataArray(jsonArray);
    }

    public <T> T get(String key, Class<T> valueClass)
    {
        var object = get(key);
        return valueClass.isInstance(object)
                ? valueClass.cast(object)
                : null;
    }

    public boolean getBoolean(String key)
    {
        var value = get(key, Boolean.class);
        return value != null && value;
    }

    public Number getNumber(String key)
    {
        return get(key, Number.class);
    }

    public String getString(String key)
    {
        return get(key, String.class);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass)
    {
        var enumString = this.getString(key);
        if (enumString != null)
        {
            try
            {
                return Enum.valueOf(enumClass, enumString);
            } catch (IllegalArgumentException exception)
            {
                return null;
            }
        }

        return null;
    }


    public <V> void getParsable(SimpleJSONObjectParser<V> objectParser, String key, V value)
    {
        if (value == null)
        {
            return;
        }

        var parsableJSONDataMap = this.getMap(key);
        if (parsableJSONDataMap != null)
        {
            objectParser.deserialize(value, parsableJSONDataMap);
        }
    }

    public <V> V getParsable(SuppliedJSONObjectParser<V> objectParser, String key)
    {
        var parsableJSONDataMap = this.getMap(key);
        if (parsableJSONDataMap != null)
        {
            return objectParser.create(parsableJSONDataMap);
        }

        return null;
    }

    public Map<String, JSONDataMap> getOnlyMaps()
    {
        var map = new HashMap<String, JSONDataMap>();
        json.forEach((key, value) ->
        {
            if(value instanceof JSONObject)
            {
                var jsonObject = (JSONObject) value;
                map.put(key.toString(), new JSONDataMap(jsonObject));
            }
        });
        return map;
    }

    public void forEach(BiConsumer<String, Object> consumer)
    {
        json.forEach((key, value) -> consumer.accept(key.toString(), value));
    }

    public int size()
    {
        return json.size();
    }

    public boolean isEmpty()
    {
        return json.isEmpty();
    }
}
