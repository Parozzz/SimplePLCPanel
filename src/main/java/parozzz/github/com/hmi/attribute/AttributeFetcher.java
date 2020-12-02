package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.state.WrapperState;

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

    public static boolean hasAttribute(ControlWrapper<?> controlWrapper, AttributeType<?> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeManager();
        return attributeManager.hasType(attributeType);
    }

    private AttributeFetcher() {}
}
