package parozzz.github.com.hmi.attribute.impl.address;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

public class ReadAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "READ_ADDRESS_ATTRIBUTE";
    public ReadAddressAttribute(ControlWrapper<?> controlWrapper)
    {
        super(controlWrapper, ATTRIBUTE_NAME, ReadAddressAttribute::new);
    }
}
