package parozzz.github.com.simpleplcpanel.hmi.comm;

import parozzz.github.com.simpleplcpanel.Nullable;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.ModbusAttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.SiemensS7AttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.AttributePropertyManager;
import parozzz.github.com.simpleplcpanel.hmi.attribute.property.impl.FunctionAttributeProperty;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressCreatorStage;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress.SiemensS7StringAddressCreator;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress.SiemensS7StringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.serialize.JSONSerializable;
import parozzz.github.com.simpleplcpanel.hmi.serialize.data.JSONDataMap;
import parozzz.github.com.simpleplcpanel.logger.Loggable;
import parozzz.github.com.simpleplcpanel.logger.MainLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CommunicationType<ASD extends CommunicationStringAddressData> implements Loggable
{
    public static final CommunicationType<SiemensS7StringAddressData> SIEMENS_S7
            = create("SIEMENS",
            SiemensS7StringAddressData::new, SiemensS7StringAddressData::parseStringData,
            SiemensS7StringAddressCreator::new
    );
    public static final CommunicationType<ModbusStringAddressData> MODBUS_TCP
            = create("MODBUS_TCP",
            ModbusStringAddressData::new, ModbusStringAddressData::parseStringData,
            ModbusStringAddressCreatorStage::new
    );
    public static final CommunicationType<?> NONE = create("NONE",
            CommunicationStringAddressData.NoneStringAddressData::new,
            CommunicationStringAddressData.NoneStringAddressData::new,
            () -> null);

    private static Map<String, CommunicationType<?>> DATA_TYPE_NAME_MAP;

    private static <ASD extends CommunicationStringAddressData> CommunicationType<ASD> create(
            String name,
            Supplier<ASD> defaultStringAddressDataSupplier, Function<String, ASD> parseStringAddressDataFunction,
            StringAddressCreatorStageSupplier<ASD> stringAddressCreatorStageSupplier)
    {
        var dataType = new CommunicationType<>(name,
                defaultStringAddressDataSupplier, parseStringAddressDataFunction, stringAddressCreatorStageSupplier
        );
        if (DATA_TYPE_NAME_MAP == null)
        {
            DATA_TYPE_NAME_MAP = new HashMap<>();
        }

        DATA_TYPE_NAME_MAP.put(name.toUpperCase(), dataType);
        return dataType;
    }

    public static CommunicationType<?> getByName(String name)
    {
        return name == null ? null : DATA_TYPE_NAME_MAP.get(name.toUpperCase());
    }

    public static CommunicationType<?>[] values()
    {
        return DATA_TYPE_NAME_MAP.values().toArray(CommunicationType[]::new);
    }

    private final String name;
    private final Supplier<ASD> defaultStringAddressDataSupplier;
    private final Function<String, ASD> parseStringAddressDataFunction;
    private final StringAddressCreatorStageSupplier<ASD> stringAddressCreatorStageSupplier;

    private CommunicationType(String name,
            Supplier<ASD> defaultStringAddressDataSupplier, Function<String, ASD> parseStringAddressDataFunction,
            StringAddressCreatorStageSupplier<ASD> stringAddressCreatorStageSupplier)
    {
        this.name = name;
        this.defaultStringAddressDataSupplier = defaultStringAddressDataSupplier;
        this.parseStringAddressDataFunction = parseStringAddressDataFunction;
        this.stringAddressCreatorStageSupplier = stringAddressCreatorStageSupplier;
    }

    public String getName()
    {
        return name;
    }

    public ASD supplyDefaultStringAddressData() //This is required since the "ReadOnly" flag is not saved inside the string address.
    {
        return defaultStringAddressDataSupplier.get();
    }

    public ASD parseStringAddressData(String stringData) //This is required since the "ReadOnly" flag is not saved inside the string address.
    {
        return parseStringAddressDataFunction.apply(stringData);
    }

    @Nullable
    public CommunicationStringAddressCreatorStage<ASD> supplyStringAddressCreatorStage()
    {
        try
        {
            var stringAddressCreatorStage = stringAddressCreatorStageSupplier.supply();
            if (stringAddressCreatorStage != null)
            {
                stringAddressCreatorStage.setup();
                stringAddressCreatorStage.setDefault();
                return stringAddressCreatorStage;
            }
        } catch (IOException exception)
        {
            MainLogger.getInstance().error("Something went wrong while suppling a new StringAddressCreatorStage", exception, this);
        }

        return null;
    }
/*
    public boolean updateAddressAttributeWithStringData(String stringData, AddressAttribute addressAttribute)
    {
        var stringAddressData = parseStringAddressDataFunction.apply(stringData);
        if (stringAddressData != null && stringAddressData.validate())
        {
            addressAttribute.setValue(attributeProperty, stringAddressData);
            return true;
        }

        return false;
    }

    @Nullable
    public AttributeProperty<ASD> getAttributeProperty()
    {
        return attributeProperty;
    }
*/
    @Override
    public String log()
    {
        return "name: " + name;
    }

    @FunctionalInterface
    public interface StringAddressCreatorStageSupplier<ASD extends CommunicationStringAddressData>
    {
        CommunicationStringAddressCreatorStage<ASD> supply() throws IOException;
    }

}

/*

    public static final CommunicationType SIEMENS_S7
            = create("SIEMENS", SiemensS7StringAddressData::new,
            SiemensS7StringAddressCreator::new,
            SiemensS7AttributePropertyHolder.ATTRIBUTE_PROPERTY_LIST
    );
    public static final CommunicationType MODBUS_TCP
            = create("MODBUS_TCP", ModbusStringAddressData::new,
            ModbusStringAddressCreatorStage::new,
            ModbusAttributePropertyHolder.ATTRIBUTE_PROPERTY_LIST
    );
    public static final CommunicationType NONE = create("NONE", () -> null, () -> null, List.of());

    private static Map<String, CommunicationType> DATA_TYPE_NAME_MAP;

    private static  CommunicationType create(
            String name, Supplier<CommunicationStringAddressData> stringAddressDataSupplier,
            StringAddressCreatorStageSupplier stringAddressCreatorStageSupplier,
            List<AttributeProperty<?>> attributePropertyList
    )
    {
        var dataType = new CommunicationType(name, stringAddressDataSupplier,
                stringAddressCreatorStageSupplier, attributePropertyList
        );

        if (DATA_TYPE_NAME_MAP == null)
        {
            DATA_TYPE_NAME_MAP = new HashMap<>();
        }

        DATA_TYPE_NAME_MAP.put(name.toUpperCase(), dataType);
        return dataType;
    }

    public static CommunicationType getByName(String name)
    {
        return name == null ? null : DATA_TYPE_NAME_MAP.get(name.toUpperCase());
    }

    public static CommunicationType[] values()
    {
        return DATA_TYPE_NAME_MAP.values().toArray(CommunicationType[]::new);
    }

    private final String name;
    private final Supplier<CommunicationStringAddressData> stringAddressDataSupplier;
    private final StringAddressCreatorStageSupplier stringAddressCreatorStageSupplier;
    private final List<AttributeProperty<?>> attributePropertyList;

    private CommunicationType(String name,
            Supplier<CommunicationStringAddressData> stringAddressDataSupplier,
            StringAddressCreatorStageSupplier stringAddressCreatorStageSupplier,
            List<AttributeProperty<?>> attributePropertyList)
    {
        this.name = name;
        this.stringAddressDataSupplier = stringAddressDataSupplier;
        this.stringAddressCreatorStageSupplier = stringAddressCreatorStageSupplier;
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

    @Nullable
    public CommunicationStringAddressData supplyStringAddressData(
            boolean readOnly) //This is required since the "ReadOnly" flag is not saved inside the string address.
    {
        var stringAddressData = stringAddressDataSupplier.get();
        if (stringAddressData != null)
        {
            stringAddressData.setReadOnly(readOnly);
        }
        return stringAddressData;
    }

    @Nullable
    public CommunicationStringAddressCreatorStage<?> supplyStringAddressCreatorStage()
    {
        try
        {
            var stringAddressCreatorStage = stringAddressCreatorStageSupplier.supply();
            if(stringAddressCreatorStage != null)
            {
                stringAddressCreatorStage.setup();
                stringAddressCreatorStage.setDefault();
                return stringAddressCreatorStage;
            }
        }
        catch (IOException exception)
        {
            MainLogger.getInstance().error("Something went wrong while suppling a new StringAddressCreatorStage", exception, this);
        }

        return null;
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

    @Override
    public String log()
    {
        return "name: " + name;
    }

    @FunctionalInterface
    public interface StringAddressCreatorStageSupplier
    {
        CommunicationStringAddressCreatorStage<?> supply() throws IOException;
    }

 */