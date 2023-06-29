package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.impl;

import javafx.scene.control.Label;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.LabeledWrapper;
import parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.attributes.ControlWrapperAttributeInitializer;

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
