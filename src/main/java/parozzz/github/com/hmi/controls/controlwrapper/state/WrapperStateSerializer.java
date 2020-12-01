package parozzz.github.com.hmi.controls.controlwrapper.state;

import parozzz.github.com.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.logger.MainLogger;

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
        jsonDataMap.set("SecondCompareType", wrapperState.getSecondCompareType());
        jsonDataMap.set("SecondCompare", wrapperState.getSecondCompare());
        jsonDataMap.set("AttributeMap", wrapperState.getAttributeMap());

        return jsonDataMap;
    }

    public static void deserializeAndAddState(WrapperStateMap wrapperStateMap, JSONDataMap jsonDataMap)
    {
        var firstCompare = jsonDataMap.getNumber("FirstCompare").intValue();
        var firstCompareType = jsonDataMap.getEnum("FirstCompareType", WrapperState.CompareType.class);

        var secondCompare = jsonDataMap.getNumber("SecondCompare").intValue();
        var secondCompareType = jsonDataMap.getEnum("SecondCompareType", WrapperState.CompareType.class);

        //This method here already initialize all the attributes!
        var wrapperState = wrapperStateMap.createState(
                firstCompare, firstCompareType,
                secondCompare, secondCompareType
        );
        MainLogger.getInstance().error("Deserializing a wrapper state has created a duplicate", new IllegalStateException(), null);
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
