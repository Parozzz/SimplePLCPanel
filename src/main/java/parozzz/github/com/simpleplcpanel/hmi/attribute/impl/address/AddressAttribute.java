package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.CommunicationTagAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.EnumAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.primitives.StringAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress.SiemensS7StringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.Tag;

import java.util.function.Function;

public abstract class AddressAttribute extends Attribute
{
    public AddressAttribute(AttributeMap attributeMap,
            AttributeType<? extends AddressAttribute> attributeType, String name)
    {
        super(attributeMap, attributeType, name);
    }

    @Nullable
    public CommunicationTag getCommunicationTag()
    {
        return this.getValue(this.getTagAttributeProperty());
    }

    public void setCommunicationTag(CommunicationTag tag)
    {
        this.setValue(this.getTagAttributeProperty(), tag);
    }

    public abstract AttributeProperty<CommunicationTag> getTagAttributeProperty();
}
