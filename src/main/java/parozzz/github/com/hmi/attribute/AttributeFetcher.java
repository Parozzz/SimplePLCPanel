package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

public final class AttributeFetcher
{
    public static <A extends Attribute> A fetch(ControlWrapper<?> controlWrapper, AttributeType<A> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeManager();

        AttributeMap attributeMap;
        if(attributeManager.hasStateType(attributeType))
        {
            attributeMap = controlWrapper.getStateMap().getCurrentState().getAttributeMap();
        }
        else if(attributeManager.hasGlobalType(attributeType))
        {
            attributeMap = controlWrapper.getGlobalAttributeMap();
        }
        else
        {
            return null;
        }

        return attributeMap.get(attributeType);
    }

    /*
    public static <A extends Attribute> A fetch(ControlWrapper<?> controlWrapper, WrapperState wrapperState,
            Class<A> attributeClass)
    {
        var stateAttributeMap = wrapperState.getAttributeMap();
        var genericAttributeMap = controlWrapper.getGlobalAttributeMap();
        return fetch(stateAttributeMap, genericAttributeMap, attributeClass);
    }

    public static <A extends Attribute> A fetch(ControlWrapper<?> controlWrapper, Class<A> attributeClass)
    {
        var stateAttributeMap = controlWrapper.getStateMap().getCurrentState().getAttributeMap();
        var genericAttributeMap = controlWrapper.getGlobalAttributeMap();
        return fetch(stateAttributeMap, genericAttributeMap, attributeClass);
    }

    public static <A extends Attribute> A fetch(WrapperState state, Class<A> attributeClass)
    {
        var stateAttributeMap = state.getAttributeMap();
        return fetch(stateAttributeMap, null, attributeClass);
    }

    public static <A extends Attribute> A fetch(AttributeMap stateAttributeMap, Class<A> attributeClass)
    {
        return fetch(stateAttributeMap, null, attributeClass);
    }

    private static <A extends Attribute> A fetch(AttributeMap stateAttributeMap, AttributeMap globalAttributeMap,
            Class<A> attributeClass)
    {
        if(globalAttributeMap != null)
        {
            var attribute = globalAttributeMap.getAttribute(attributeClass);
            if(attribute != null)
            {
                return attribute;
            }
        }

        Objects.requireNonNull(stateAttributeMap, "Trying to fetch an attribute but the StateAttributeMap is null");
        return stateAttributeMap.getAttribute(attributeClass);
    }
*/
    public static boolean hasAttribute(ControlWrapper<?> controlWrapper, AttributeType<?> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeManager();
        return attributeManager.hasType(attributeType);
    }

    private AttributeFetcher() {}
}
