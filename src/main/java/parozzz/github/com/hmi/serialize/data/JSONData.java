package parozzz.github.com.hmi.serialize.data;

import org.json.simple.JSONAware;
import org.json.simple.JSONStreamAware;
import parozzz.github.com.hmi.serialize.JSONSerializable;
import parozzz.github.com.hmi.serialize.JSONSerializables;

import java.util.Objects;

public abstract class JSONData<J extends JSONAware & JSONStreamAware>
{
    protected final J json;

    public JSONData(J json)
    {
        this.json = json;
    }

    public J getJson()
    {
        return json;
    }

    protected Object parseSetObject(Object object)
    {
        Objects.requireNonNull(object, "Trying to parse an object for serialization but is null");
        var setObjectClass = object.getClass();

        if (object instanceof JSONData<?>)
        {
            object = ((JSONData<?>) object).getJson();
        } else if (object instanceof JSONSerializable)
        {
            object = ((JSONSerializable) object).serialize().getJson();
            Objects.requireNonNull(object, () -> "A JSONSerializable is returning null on serialize. Class: " + setObjectClass);
        } else if (object instanceof Enum<?>)
        {
            object = ((Enum<?>) object).name();
        }

        var jsonParser = JSONSerializables.getParserFromClass(object.getClass());
        if(jsonParser != null)
        {
            var serializedObject = jsonParser.serializeObject(object);
            if(serializedObject != null)
            {
                object = serializedObject;
            }
        }

        return object;
    }
}
