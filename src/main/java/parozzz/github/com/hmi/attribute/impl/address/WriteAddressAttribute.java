package parozzz.github.com.hmi.attribute.impl.address;

import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;

public class WriteAddressAttribute extends AddressAttribute
{
    public static final String ATTRIBUTE_NAME = "WRITE_ADDRESS_ATTRIBUTE";
    public WriteAddressAttribute(ControlWrapper<?> controlWrapper)
    {
        super(controlWrapper, ATTRIBUTE_NAME, WriteAddressAttribute::new);
    }
}
