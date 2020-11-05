package parozzz.github.com.hmi.serialize;

public abstract class FXSerializableObject
{
    /*
    private final static Map<String, Field> cachedAnnotatedFieldMap = new HashMap<>();
    private static boolean dataCached = false;

    private void cacheData()
    {
        if (!dataCached)
        {
            for (var field : this.getClass().getDeclaredFields())
            {
                var annotation = field.getAnnotation(FXSerializable.class);
                if (annotation != null)
                {
                    var fieldName = annotation.name();
                    if (fieldName.isEmpty())
                    {
                        fieldName = field.getName();
                    }

                    Validate.needTrue("Two serializable field with same name", cachedAnnotatedFieldMap.put(fieldName, field) == null);
                }
            }

            dataCached = true;
        }
    }

    private Object serializeObject(Class<?> objectClass, Object object) throws IllegalAccessException
    {
        if (FXSerializableObject.class.isAssignableFrom(objectClass))
        {
            var fxSerializableObject = (FXSerializableObject) object;
            return fxSerializableObject.serialize();
        } else if (objectClass.isPrimitive() || Number.class.isAssignableFrom(objectClass))
        {
            return object;
        } else if (ObservableValue.class.isAssignableFrom(objectClass))
        {
            var observableValue = (ObservableValue<?>) object;

            var value = observableValue.getValue();
            return serializeObject(value.getClass(), value);
        } else if (String.class.isAssignableFrom(objectClass))
        {
            return object;
        } else if (Enum.class.isAssignableFrom(objectClass))
        {
            return ((Enum<?>) object).name();
        }

        JSONObjectParser jsonParser = JSONSerializables.getParserFromClass(objectClass);
        if (jsonParser != null)
        {
            if (jsonParser.getValueClass().isInstance(object))
            {
                return jsonParser.serialize(object);
            }
        }

        return null;
    }

    public JSONDataMap serialize() throws IllegalAccessException
    {
        cacheData();

        var jsonDataMap = new JSONDataMap();

        for (var entry : cachedAnnotatedFieldMap.entrySet())
        {
            var fieldName = entry.getKey();
            var field = entry.getValue();

            var object = field.get(this);
            if (object == null)
            {
                continue;
            }

            var fieldType = field.getType();

            var serializedObject = this.serializeObject(fieldType, object);
            if (serializedObject == null)
            {
                Logger.getLogger(FXSerializableObject.class.getSimpleName()).log(Level.WARNING,
                        "An object has been found without serialization. Class: " + fieldType.getSimpleName() + ". Name: " + fieldName);
                continue;
            }

            jsonDataMap.set(fieldName, object);
        }

        return jsonDataMap;
    }

    public void deserialize(JSONDataMap jsonDataMap)
    {
        cacheData();

        for (var entry : cachedAnnotatedFieldMap.entrySet())
        {
            var fieldName = entry.getKey();
            var field = entry.getValue();

            var objectClass = field.getType();
            if ((boolean.class.isAssignableFrom(objectClass) || Boolean.class.isAssignableFrom(objectClass))
                    || (char.class.isAssignableFrom(objectClass) || Character.class.isAssignableFrom(objectClass)))
            {
                return object;
            }
            else if (SerializeUtil.isPrimitiveNumber(objectClass))
            {

            }

            if (String.class.isAssignableFrom(objectClass))
            {
                return object;
            }
        }
    }
*/
}
