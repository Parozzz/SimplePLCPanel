package parozzz.github.com.hmi.controls.controlwrapper.state;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;

import java.util.function.Consumer;

final class WrapperStateSerializer
{
    public static final String WRAPPER_DEFAULT_STATE_STRING = "YeahBoiiiiiiiiii";
    public static JSONDataMap serializeDefaultState(WrapperState defaultWrapperState)
    {
        var jsonDataMap = new JSONDataMap();

        jsonDataMap.set("WrapperDefaultState", WRAPPER_DEFAULT_STATE_STRING);
        jsonDataMap.set("AttributeMap", defaultWrapperState.getAttributeMap());

        return jsonDataMap;
    }

    public static void deserializeDefaultState(JSONDataMap jsonDataMap, WrapperState defaultWrapperState)
    {
        var wrapperType = jsonDataMap.getString("WrapperDefaultState");
        if (WRAPPER_DEFAULT_STATE_STRING.equals(wrapperType))
        {
            deserializeAttributeMap(jsonDataMap, defaultWrapperState);
        }
    }

    public static JSONDataMap serialize(WrapperState wrapperState)
    {
        var jsonDataMap = new JSONDataMap();

        jsonDataMap.set("FirstCompareType", wrapperState.getFirstCompareType());
        jsonDataMap.set("FirstCompare", wrapperState.getFirstCompare());
        jsonDataMap.set("SecondCompareType", wrapperState.getFirstCompareType());
        jsonDataMap.set("SecondCompare", wrapperState.getSecondCompare());
        jsonDataMap.set("AttributeMap", wrapperState.getAttributeMap());

        return jsonDataMap;
    }

    public static void deserialize(JSONDataMap jsonDataMap, Consumer<WrapperState> addStatePredicate)
    {
        var firstCompare = jsonDataMap.getNumber("FirstCompare").intValue();
        var firstCompareType = jsonDataMap.getEnum("FirstCompareType", WrapperState.CompareType.class);

        var secondCompare = jsonDataMap.getNumber("SecondCompare").intValue();
        var secondCompareType = jsonDataMap.getEnum("SecondCompareType", WrapperState.CompareType.class);

        var wrapperState = new WrapperState(firstCompare, firstCompareType, secondCompare, secondCompareType);
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
