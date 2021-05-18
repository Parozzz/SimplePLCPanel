package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address;

import parozzz.github.com.simpleplcpanel.hmi.attribute.Attribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeMap;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
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

    public static final AttributeProperty<AddressSetupPane.AddressType> ADDRESS_TYPE =
            new EnumAttributeProperty<>("CommunicationType", AddressSetupPane.AddressType.NONE);

    public static final AttributeProperty<SiemensS7StringAddressData> SIEMENS_STRING_DATA =
            new FunctionAttributeProperty<>("Siemens.StringData", new SiemensS7StringAddressData(),
                    SiemensS7StringAddressData::getStringData,
                    (jsonDataMap, key) ->
                    {
                        var stringData = jsonDataMap.getString(key);
                        if(stringData != null)
                        {
                            var stringAddressData = SiemensS7StringAddressData.parseStringData(stringData);
                            return stringAddressData == null
                                    ? new SiemensS7StringAddressData()
                                    : stringAddressData;
                        }

                        return null;
                    }
            );

    public static final AttributeProperty<ModbusStringAddressData> MODBUS_TCP_STRING_DATA =
            new FunctionAttributeProperty<>("ModbusTCP.StringData", new ModbusStringAddressData(),
                    ModbusStringAddressData::getStringData,
                    (jsonDataMap, key) ->
                    {
                        var stringData = jsonDataMap.getString(key);
                        if(stringData != null)
                        {
                            var stringAddressData = ModbusStringAddressData.parseStringData(stringData);
                            return stringAddressData == null
                                    ? new ModbusStringAddressData()
                                    : stringAddressData;
                        }

                        return null;
                    }
            );

    public AddressAttribute(AttributeMap attributeMap, AttributeType<? extends AddressAttribute> attributeType,
            String name)
    {
        super(attributeMap, attributeType, name);

        super.getAttributePropertyManager().addAll(ADDRESS_TYPE, SIEMENS_STRING_DATA, MODBUS_TCP_STRING_DATA);
    }


    @Override
    public void update()
    {

    }
}
