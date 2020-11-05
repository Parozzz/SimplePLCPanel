package parozzz.github.com.hmi.serialize.data;

import org.json.simple.JSONArray;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class JSONDataArray extends JSONData<JSONArray> implements Iterable<Object>
{

    public static JSONDataArray of(Collection<?> collection)
    {
        var jsonDataArray = new JSONDataArray();
        collection.forEach(jsonDataArray::add);
        return jsonDataArray;
    }

    public JSONDataArray()
    {
        super(new JSONArray());
    }

    public JSONDataArray(JSONArray jsonArray)
    {
        super(jsonArray);
    }

    public void add(Object object)
    {
        json.add(super.parseSetObject(object));
    }

    @Override
    public Iterator<Object> iterator()
    {
        return json.iterator();
    }

    public Stream<Object> stream()
    {
        return json.stream();
    }

    public void forEach(Consumer<Object> consumer)
    {
        json.forEach(consumer);
    }

    public boolean isEmpty()
    {
        return json.isEmpty();
    }

    public int size()
    {
        return json.size();
    }
}
