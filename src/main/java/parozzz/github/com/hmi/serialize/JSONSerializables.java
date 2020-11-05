package parozzz.github.com.hmi.serialize;

import javafx.scene.paint.Color;
import parozzz.github.com.hmi.serialize.parsers.JSONObjectParser;
import parozzz.github.com.hmi.serialize.parsers.impl.ColorJSONParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class JSONSerializables
{
    private final static Map<Class<?>, JSONObjectParser<?>> objectParserMap = new HashMap<>();
    private final static Map<String, JSONObjectParser<?>> objectParserIdentifierMap = new HashMap<>();
    public static JSONObjectParser<?> getParserFromClass(Class<?> clazz)
    {
        return objectParserMap.get(clazz);
    }

    public static JSONObjectParser<?> getParserFromIdentifier(String identifier)
    {
        return objectParserIdentifierMap.get(Objects.requireNonNull(identifier));
    }

    public static final ColorJSONParser COLOR = create(new ColorJSONParser(), Color.class);

    private static <V, T extends JSONObjectParser<V>> T create(T parser, Class<V> objectClass)
    {
        objectParserIdentifierMap.put(parser.getIdentifier(), parser);
        objectParserMap.put(objectClass, parser);
        return parser;
    }
}
