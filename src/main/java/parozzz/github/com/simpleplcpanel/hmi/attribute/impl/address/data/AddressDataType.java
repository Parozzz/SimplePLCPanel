package parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data;

import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributePropertyManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AddressDataType
{

    public static final AddressDataType SIEMENS = create("SIEMENS", SiemensDataPropertyHolder.ATTRIBUTE_PROPERTY_LIST);
    public static final AddressDataType MODBUS_TCP = create("MODBUS_TCP", ModbusTCPDataPropertyHolder.ATTRIBUTE_PROPERTY_LIST);
    public static final AddressDataType NONE = create("NONE", List.of());

    private static Map<String, AddressDataType> DATA_TYPE_NAME_MAP;

    private static AddressDataType create(String name, List<AttributeProperty<?>> attributePropertyList)
    {
        var dataType = new AddressDataType(name, attributePropertyList);

        if (DATA_TYPE_NAME_MAP == null)
        {
            DATA_TYPE_NAME_MAP = new HashMap<>();
        }

        DATA_TYPE_NAME_MAP.put(name, dataType);
        return dataType;
    }

    public static AddressDataType getByName(String name)
    {
        return DATA_TYPE_NAME_MAP.get(name);
    }

    public static AddressDataType[] values()
    {
        return DATA_TYPE_NAME_MAP.values().toArray(AddressDataType[]::new);
    }

    private final String name;
    private final List<AttributeProperty<?>> attributePropertyList;

    private AddressDataType(String name,List<AttributeProperty<?>> attributePropertyList)
    {
        this.name = name;
        this.attributePropertyList = attributePropertyList;
    }

    public String getName()
    {
        return name;
    }

    public List<AttributeProperty<?>> getAttributePropertyList()
    {
        return attributePropertyList;
    }

    public void addAttributesTo(AttributePropertyManager attributePropertyManager)
    {
        if (!attributePropertyList.isEmpty())
        {
            attributePropertyManager.addAll(attributePropertyList.toArray(AttributeProperty[]::new));
        }
    }

    public void removeAttributesFrom(AttributePropertyManager attributePropertyManager)
    {
        if (!attributePropertyList.isEmpty())
        {
            attributePropertyManager.removeAll(attributePropertyList.toArray(AttributeProperty[]::new));
        }
    }
}
