package parozzz.github.com.hmi.controls.controlwrapper.state;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.util.function.Consumer;

final class WrapperStateSerializer
{
    public static JSONDataMap serializeDefaultState(WrapperState defaultWrapperState)
    {
        var jsonDataMap = new JSONDataMap();

        jsonDataMap.set("WrapperStateType", "DEFAULT_STATE");
        jsonDataMap.set("AttributeMap", defaultWrapperState.getAttributeMap());

        return jsonDataMap;
    }

    public static void deserializeDefaultState(JSONDataMap jsonDataMap, WrapperState defaultWrapperState)
    {
        var wrapperType = jsonDataMap.getString("WrapperStateType");
        if ("DEFAULT_STATE".equals(wrapperType))
        {
            deserializeAttributeMap(jsonDataMap, defaultWrapperState);
        }
    }

    public static JSONDataMap serialize(WrapperState wrapperState)
    {
        var jsonDataMap = new JSONDataMap();

        jsonDataMap.set("WrapperStateType", wrapperState.getType().name());
        jsonDataMap.set("FirstCompare", wrapperState.getFirstCompare());
        jsonDataMap.set("SecondCompare", wrapperState.getSecondCompare());
        jsonDataMap.set("AttributeMap", wrapperState.getAttributeMap());

        return jsonDataMap;
    }

    public static void deserialize(JSONDataMap jsonDataMap, Consumer<WrapperState> addStatePredicate)
    {
        var type = jsonDataMap.getEnum("WrapperStateType", WrapperState.Type.class);
        if(type == null || jsonDataMap.size() == 1)
        {
            return;
        }

        var firstCompare = jsonDataMap.getNumber("FirstCompare").intValue();
        var secondCompare = jsonDataMap.getNumber("SecondCompare").intValue();

        var wrapperState = new WrapperState(type, firstCompare, secondCompare);
        addStatePredicate.accept(wrapperState);
        deserializeAttributeMap(jsonDataMap, wrapperState);
    }

    private static void deserializeAttributeMap(JSONDataMap jsonDataMap, WrapperState wrapperState)
    {
        //And then deserialize them
        var attributeMapJSONDataMap = jsonDataMap.getMap("AttributeMap");
        if (attributeMapJSONDataMap != null)
        {
            wrapperState.getAttributeMap().deserialize(attributeMapJSONDataMap);
        }
    }

    private WrapperStateSerializer()
    {
    }
}
