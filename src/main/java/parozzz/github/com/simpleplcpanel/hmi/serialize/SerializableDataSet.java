package parozzz.github.com.simpleplcpanel.hmi.serialize;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.serialize.parsers.SuppliedJSONObjectParser;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.SerializableProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl.SerializableParsedProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl.SerializableReadOnlyProperty;
import parozzz.github.com.simpleplcpanel.hmi.serialize.property.impl.SerializableSimpleProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SerializableDataSet implements JSONSerializable
{
    private final static Logger logger = Logger.getLogger(SerializableDataSet.class.getSimpleName());

    private final Set<SerializableProperty<?>> serializablePropertySet;
    private final Map<String, JSONSerializable> serializableMap;

    public SerializableDataSet()
    {
        serializablePropertySet = new HashSet<>();
        this.serializableMap = new HashMap<>();
    }

    public SerializableDataSet addReadOnlyBoolean(String key, ReadOnlyProperty<Boolean> property,
            Consumer<Boolean> setterConsumer)
    {
        return add(new SerializableReadOnlyProperty<>(key, property, setterConsumer, Function.identity(), JSONDataMap::getBoolean));
    }

    public SerializableDataSet addReadOnlyInt(String key, ReadOnlyProperty<Number> property,
            Consumer<Integer> setterConsumer)
    {
        return add(new SerializableReadOnlyProperty<>(key, property, setterConsumer, Number::intValue, JSONDataMap::getNumber));
    }

    public SerializableDataSet addReadOnlyDouble(String key, ReadOnlyProperty<Number> property,
            Consumer<Double> setterConsumer)
    {
        return add(new SerializableReadOnlyProperty<>(key, property, setterConsumer, Number::doubleValue, JSONDataMap::getNumber));
    }

    public SerializableDataSet addBoolean(String key, Property<Boolean> property)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getBoolean)
                        .build()
        );
       //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getBoolean, null));
    }

    public SerializableDataSet addBoolean(String key, Property<Boolean> property, boolean loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getBoolean)
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );
        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getBoolean, loadDefaultValue));
    }

    public SerializableDataSet addInt(String key, Property<Number> property)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getNumber, Number::intValue)
                        .build()
        );
        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getNumber, Number::intValue, null));
    }

    public SerializableDataSet addInt(String key, Property<Number> property, int loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getNumber, Number::intValue)
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );
        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getNumber, Number::intValue, loadDefaultValue));
    }

    public SerializableDataSet addDouble(String key, Property<Number> property)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getNumber, Number::doubleValue)
                        .build()
        );
        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getNumber, Number::doubleValue, null));
    }

    public SerializableDataSet addDouble(String key, Property<Number> property, double loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getNumber, Number::doubleValue)
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );
        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getNumber, Number::doubleValue, loadDefaultValue));
    }

    public SerializableDataSet addString(String key, Property<String> property)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getString)
                        .build()
        );

        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getString, null));
    }

    public SerializableDataSet addString(String key, Property<String> property, String loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction(JSONDataMap::getString)
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );

        //return add(new SerializableSimpleProperty<>(key, property, JSONDataMap::getString, loadDefaultValue));
    }

    public <V extends Enum<V>> SerializableDataSet addEnum(String key, Property<V> property, Class<V> enumClass)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction((jsonDataMap, tKey) ->  jsonDataMap.getEnum(tKey, enumClass))
                        .build()
        );

        //return add(new SerializableSimpleProperty<>(key, property, (jsonDataMap, tKey) -> jsonDataMap.getEnum(tKey, enumClass), null));
    }

    public <V extends Enum<V>> SerializableDataSet addEnum(String key, Property<V> property, Class<V> enumClass,
            V loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction((jsonDataMap, tKey) ->  jsonDataMap.getEnum(tKey, enumClass))
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );

        //return add(new SerializableSimpleProperty<>(key, property, (jsonDataMap, tKey) -> jsonDataMap.getEnum(tKey, enumClass), loadDefaultValue));
    }

    public <V> SerializableDataSet addFunction(String key, Property<V> property,
            Function<String, V> parseStringFunction, Function<V, String> toStringFunction)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction((jsonDataMap, tKey) ->
                        {
                            var stringValue = jsonDataMap.getString(tKey);
                            return stringValue == null
                                    ? null
                                    : parseStringFunction.apply(stringValue);
                        })
                        .setterFunction(toStringFunction::apply)
                        .build()
        );
        /*
        return add(
                new SerializableSimpleProperty<>(key, property,
                        (jsonDataMap, tKey) -> parseStringFunction.apply(jsonDataMap))
        );*/
    }

    public <V> SerializableDataSet addFunction(String key, Property<V> property,
            Function<String, V> parseStringFunction, Function<V, String> toStringFunction,
            V loadDefaultValue)
    {
        return add(
                SerializableSimpleProperty.builder(key, property)
                        .getterFunction((jsonDataMap, tKey) ->
                        {
                            var stringValue = jsonDataMap.getString(tKey);
                            return stringValue == null
                                    ? null
                                    : parseStringFunction.apply(stringValue);
                        })
                        .setterFunction(toStringFunction::apply)
                        .loadDefaultValue(loadDefaultValue)
                        .build()
        );
        /*
        return add(
                new SerializableSimpleProperty<>(key, property,
                        (jsonDataMap, tKey) -> parseStringFunction.apply(jsonDataMap))
        );*/
    }

    public <V> SerializableDataSet addParsable(String key, Property<V> property,
            SuppliedJSONObjectParser<V> objectParser)
    {
        return add(new SerializableParsedProperty<>(key, property, objectParser));
    }

    public SerializableDataSet add(SerializableProperty<?> serializableProperty)
    {
        serializablePropertySet.add(serializableProperty);
        return this;
    }

    public SerializableDataSet addSerializable(String key, JSONSerializable serializable)
    {
        if (serializable != null)
        {
            serializableMap.put(key, serializable);
        }

        return this;
    }

    @Override
    public JSONDataMap serialize()
    {
        var jsonDataMap = new JSONDataMap();
        this.serializeInto(jsonDataMap);
        return jsonDataMap;
    }

    public void serializeInto(JSONDataMap jsonDataMap)
    {
        for (var serializableProperty : serializablePropertySet)
        {
            serializableProperty.set(jsonDataMap);
        }

        for (var entry : serializableMap.entrySet())
        {
            var jsonSerializable = entry.getValue();
            if (jsonSerializable == null)
            {
                logger.log(Level.WARNING, "Trying to serialize an JSONSerializable but is null");
                continue;
            }

            jsonDataMap.set(entry.getKey(), jsonSerializable);
        }
    }

    @Override
    public void deserialize(JSONDataMap jsonDataMap)
    {
        for (var serializableProperty : serializablePropertySet)
        {
            serializableProperty.load(jsonDataMap);
        }

        for (var entry : serializableMap.entrySet())
        {
            var jsonSerializable = entry.getValue();
            if (jsonSerializable == null)
            {
                logger.log(Level.WARNING, "Trying to de-serialize an JSONSerializable but is null");
                continue;
            }

            var serializableJSONDataMap = jsonDataMap.getMap(entry.getKey());
            if (serializableJSONDataMap == null)
            {
                logger.log(Level.WARNING, "Trying to de-serialize an JSONSerializable but it has no JSONDataMap. Class: "
                        + jsonSerializable.getClass().getSimpleName());
                continue;
            }

            jsonSerializable.deserialize(serializableJSONDataMap);
        }
    }
}
