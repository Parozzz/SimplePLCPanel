package parozzz.github.com.hmi.attribute;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

public final class AttributeFetcher
{
    public static <A extends Attribute> A fetch(ControlWrapper<?> controlWrapper, AttributeType<A> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeTypeManager();

        AttributeMap attributeMap;
        if(attributeManager.isState(attributeType))
        {
            attributeMap = controlWrapper.getStateMap().getCurrentState().getAttributeMap();
        }
        else if(attributeManager.isGlobal(attributeType))
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
        var attributeManager = controlWrapper.getAttributeTypeManager();
        return attributeManager.hasType(attributeType);
    }

    private AttributeFetcher() {}
}
