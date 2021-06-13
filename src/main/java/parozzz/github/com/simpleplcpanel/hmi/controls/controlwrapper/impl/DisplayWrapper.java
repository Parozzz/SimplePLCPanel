package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.impl;

import javafx.scene.control.Label;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.LabeledWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.attributes.ControlWrapperAttributeInitializer;

public class DisplayWrapper extends LabeledWrapper<Label>
{
    public DisplayWrapper(ControlContainerPane controlContainerPane)
    {
        super(controlContainerPane, ControlWrapperType.DISPLAY, false);
    }

    @Override
    protected void registerAttributeInitializers(ControlWrapperAttributeInitializer<Label> attributeInitializer)
    {
        super.registerAttributeInitializers(attributeInitializer);
    }

    @Override
    public ControlWrapper<?> createInstance()
    {
        return new DisplayWrapper(super.getControlMainPage());
    }

}
