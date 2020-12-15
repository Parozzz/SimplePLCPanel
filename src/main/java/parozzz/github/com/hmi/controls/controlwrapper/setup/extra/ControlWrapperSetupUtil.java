package parozzz.github.com.hmi.controls.controlwrapper.setup.extra;

import javafx.beans.property.Property;
import parozzz.github.com.hmi.attribute.Attribute;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.controls.controlwrapper.setup.attributechanger.SetupPaneAttributeChangerList;

public final class ControlWrapperSetupUtil
{
    public static <A extends Attribute> void writeAttributeChangerListToAllStates(
            ControlWrapper<?> controlWrapper,
             SetupPaneAttributeChangerList<A> attributeChangerList)
    {
        var attributeType = attributeChangerList.getAttributeType();
        //Set the changed data to ALL the states of the wrapper
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            var attribute = wrapperState.getAttributeMap().get(attributeType);
            if (attribute != null)
            {//This is ok. A single attribute changer just modify one single data inside an attribute (I know naming is shite!)
                for(var attributeChanger : attributeChangerList)
                {
                    attributeChanger.setDataToAttribute(attribute);
                }
            }
        });
    }

    public static <A extends Attribute> void writeSingleAttributeChangerToAllStates(ControlWrapper<?> controlWrapper,
            SetupPaneAttributeChangerList<A> attributeChangerList, Property<?> property)
    {
        var attributeType = attributeChangerList.getAttributeType();
        //Set the changed data to ALL the states of the wrapper
        controlWrapper.getStateMap().forEach(wrapperState ->
        {
            var attribute = wrapperState.getAttributeMap().get(attributeType);
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
