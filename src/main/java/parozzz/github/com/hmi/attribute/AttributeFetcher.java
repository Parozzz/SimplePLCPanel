package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

import java.util.Objects;

public final class AttributeFetcher
{

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

    public static boolean hasAttribute(WrapperState wrapperState, Class<? extends Attribute> attributeClass)
    {
        return hasAttribute(wrapperState.getAttributeMap(), null, attributeClass);
    }

    public static boolean hasAttribute(AttributeMap stateAttributeMap, AttributeMap globalAttributeMap,
            Class<? extends Attribute> attributeClass)
    {
        if(globalAttributeMap != null && globalAttributeMap.hasAttribute(attributeClass))
        {
            return true;
        }

        Objects.requireNonNull(stateAttributeMap, "Trying to check \"hasAttribute\" but the StateAttributeMap is null");
        return stateAttributeMap.hasAttribute(attributeClass);
    }

    private AttributeFetcher() {}
}
