package parozzz.github.com.hmi.controls.controlwrapper.setup.extra;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;

public final class ControlWrapperSetupUtil
{
    public static <A extends Attribute> void writeAttributeChangerListToAllStates(ControlWrapper<?> controlWrapper,
            Class<A> attributeClass, SetupPaneAttributeChangerList<A> attributeChangerList)
    {
        //Set the changed data to ALL the states of the wrapper
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            var attribute = AttributeFetcher.fetch(wrapperState, attributeClass);
            if (attribute != null)
            {
                for(var attributeChanger : attributeChangerList)
                {
                    attributeChanger.setDataToAttribute(attribute);
                }
            }
        });
    }

    public static <A extends Attribute> void writeSingleAttributeChangerToAllStates(ControlWrapper<?> controlWrapper,
            Class<A> attributeClass, SetupPaneAttributeChangerList<A> attributeChangerList,
            Property<?> property)
    {
        //Set the changed data to ALL the states of the wrapper
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            var attribute = AttributeFetcher.fetch(wrapperState, attributeClass);
            if (attribute != null)
            {
                var attributeChanger = attributeChangerList.getByProperty(property);
                if (attributeChanger != null)
                {
                    attributeChanger.setDataToAttribute(attribute);
                }
            }
        });
    }
}
