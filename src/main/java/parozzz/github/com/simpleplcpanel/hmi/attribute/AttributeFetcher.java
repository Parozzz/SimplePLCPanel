package parozzz.github.com.simpleplcpanel.hmi.attribute;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state.WrapperState;

public final class AttributeFetcher
{
    public static <A extends Attribute> A fetch(ControlWrapper<?> controlWrapper, AttributeType<A> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeTypeManager();

        AttributeMap attributeMap;
        if (attributeManager.isState(attributeType))
        {
            attributeMap = controlWrapper.getStateMap().getCurrentState().getAttributeMap();
        } else if (attributeManager.isGlobal(attributeType))
        {
            attributeMap = controlWrapper.getGlobalAttributeMap();
        } else
        {
            return null;
        }

        return attributeMap.get(attributeType);
    }

    @Nullable
    public static <A extends Attribute> FetchResult<A> fetchResult(
            ControlWrapper<?> controlWrapper, AttributeType<A> attributeType
    )
    {
        var attributeManager = controlWrapper.getAttributeTypeManager();

        if (attributeManager.isState(attributeType))
        {
            var wrapperState = controlWrapper.getStateMap().getCurrentState();
            var attributeMap = wrapperState.getAttributeMap();

            var attribute = attributeMap.get(attributeType);
            if(attribute != null)
            {
                return new FetchResult<>(attribute, attributeMap, false, wrapperState);
            }
        } else if (attributeManager.isGlobal(attributeType))
        {
            var attributeMap = controlWrapper.getGlobalAttributeMap();

            var attribute = attributeMap.get(attributeType);
            if(attribute != null)
            {
                return new FetchResult<>(attribute, attributeMap, false, null);
            }
        }

        return null;
    }

    public static boolean hasAttribute(ControlWrapper<?> controlWrapper, AttributeType<?> attributeType)
    {
        var attributeManager = controlWrapper.getAttributeTypeManager();
        return attributeManager.hasType(attributeType);
    }

    private AttributeFetcher() {}

    public static class FetchResult<A extends Attribute>
    {
        private final A attribute;
        private final AttributeMap attributeMap;
        private final boolean global;
        private final WrapperState wrapperState;

        public FetchResult(A attribute, AttributeMap attributeMap, boolean global, @Nullable WrapperState wrapperState)
        {
            this.attribute = attribute;
            this.attributeMap = attributeMap;
            this.global = global;
            this.wrapperState = wrapperState;
        }

        public A getAttribute()
        {
            return attribute;
        }

        public AttributeMap getAttributeMap()
        {
            return attributeMap;
        }

        public boolean isGlobal()
        {
            return global;
        }

        @Nullable
        public WrapperState getWrapperState()
        {
            return wrapperState;
        }
    }
}
