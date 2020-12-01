package parozzz.github.com.hmi.database;

import parozzz.github.com.hmi.attribute.AttributeFetcher;
import parozzz.github.com.hmi.attribute.AttributeType;
import parozzz.github.com.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.ReadAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.WriteAddressAttribute;
import parozzz.github.com.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.hmi.attribute.impl.address.data.ModbusTCPDataPropertyHolder;
import parozzz.github.com.hmi.comm.ReadOnlyIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.ModbusTCPThread;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPReadNumberIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.bit.ModbusTCPReadBitIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.bit.ModbusTCPWriteBitIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.doubleword.ModbusTCPReadDWordIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.doubleword.ModbusTCPWriteDWordIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.quadword.ModbusTCPReadQWordIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.quadword.ModbusTCPWriteQWordIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.word.ModbusTCPReadWordIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.word.ModbusTCPWriteWordIntermediate;
import parozzz.github.com.hmi.util.valueintermediate.ValueIntermediate;

import java.util.Set;
import java.util.stream.Stream;

public final class ModbusTCPDataUpdater extends ControlDataUpdater<ModbusTCPThread>
{
    public ModbusTCPDataUpdater(ControlContainerDatabase controlContainerDatabase, ModbusTCPThread commThread)
    {
        super(controlContainerDatabase, commThread);
    }

    @Override
    public void parseReadData()
    {
        Stream.of(commThread.getReadCoilSet(),
                commThread.getReadDiscreteInputsSet(),
                commThread.getReadHoldingRegisterSet(),
                commThread.getReadInputRegistersSet()).forEach(this::parseReadOnlySet);
    }

    private void parseReadOnlySet(Set<? extends ReadOnlyIntermediate> intermediateSet)
    {
        if (intermediateSet != null)
        {
            intermediateSet.forEach(ReadOnlyIntermediate::parse);
        }
    }

    @Override
    public void update()
    {
        if (commThread.isUpdating())
        {
            return;
        }

        var readHoldingRegisterSet = commThread.getReadHoldingRegisterSet();
        readHoldingRegisterSet.clear();

        var writeHoldingRegisterSet = commThread.getWriteHoldingRegisterSet();
        writeHoldingRegisterSet.clear();

        var writeCoilsSet = commThread.getWriteCoilSet();
        writeCoilsSet.clear();

        var readCoilsSet = commThread.getReadCoilSet();
        readCoilsSet.clear();

        var readDiscreteInputsSet = commThread.getReadDiscreteInputsSet();
        readDiscreteInputsSet.clear();

        var readInputRegistersSet = commThread.getReadInputRegistersSet();
        readInputRegistersSet.clear();

        for (var controlWrapper : controlContainerDatabase.getControlWrapperSet())
        {
            if (newValueControlWrapperSet.remove(controlWrapper))
            {
                var writeCachedData = this.getCachedData(AttributeFetcher.fetch(controlWrapper, AttributeType.WRITE_ADDRESS));
                if (writeCachedData != null)
                {
                    var offset = writeCachedData.getOffset();
                    var internalValue = controlWrapper.getValue().getInternalValue();
                    switch (writeCachedData.getFunctionCode())
                    {
                        case HOLDING_REGISTER:
                            switch (writeCachedData.getDataLength())
                            {
                                case WORD:
                                    writeHoldingRegisterSet.add(new ModbusTCPWriteWordIntermediate(offset, internalValue.asInteger()));
                                    break;
                                case DOUBLE_WORD:
                                    writeHoldingRegisterSet.add(new ModbusTCPWriteDWordIntermediate(offset, internalValue.asInteger()));
                                    break;
                                case QUAD_WORD:
                                    writeHoldingRegisterSet.add(new ModbusTCPWriteQWordIntermediate(offset, internalValue.asLong()));
                                    break;
                            }
                            break;
                        case COIL:
                            writeCoilsSet.add(new ModbusTCPWriteBitIntermediate(offset, internalValue.asBoolean()));
                            break;
                    }
                }

                //Parse write values here!
            }

            var readCachedData = this.getCachedData(AttributeFetcher.fetch(controlWrapper, AttributeType.READ_ADDRESS));
            if (readCachedData != null)
            {
                var offset = readCachedData.getOffset();
                var outsideValue = controlWrapper.getValue().getOutsideValue();
                switch (readCachedData.getFunctionCode())
                {
                    case HOLDING_REGISTER:
                        readHoldingRegisterSet.add(this.parseReadNumberIntermediate(readCachedData, offset, outsideValue));
                        break;
                    case COIL:
                        readCoilsSet.add(new ModbusTCPReadBitIntermediate(offset, outsideValue::setBoolean));
                        break;
                    case INPUT_REGISTER:
                        readInputRegistersSet.add(this.parseReadNumberIntermediate(readCachedData, offset, outsideValue));
                        break;
                    case DISCRETE_INPUT:
                        readDiscreteInputsSet.add(new ModbusTCPReadBitIntermediate(offset, outsideValue::setBoolean));
                        break;
                }
            }
            //Parse read values here
        }

        if (!(readHoldingRegisterSet.isEmpty() && writeHoldingRegisterSet.isEmpty()
                && writeCoilsSet.isEmpty() && readCoilsSet.isEmpty()
                && readDiscreteInputsSet.isEmpty()
                && readInputRegistersSet.isEmpty()))
        {
            commThread.doUpdate();
        }
    }

    private ModbusTCPReadNumberIntermediate parseReadNumberIntermediate(ModbusTCPDataPropertyHolder.CachedData data, int offset, ValueIntermediate intermediate)
    {
        ModbusTCPReadNumberIntermediate readNumberIntermediate;
        switch (data.getDataLength())
        {
            case BIT:
                var bitOffset = data.getBitOffset();
                readNumberIntermediate = new ModbusTCPReadWordIntermediate(offset, value ->
                {
                    var booleanValue = (value & (1 >> bitOffset)) != 0;
                    intermediate.setBoolean(booleanValue);
                });
                break;
            case WORD:
                readNumberIntermediate = new ModbusTCPReadWordIntermediate(offset, intermediate::setNumber);
                break;
            case DOUBLE_WORD:
                readNumberIntermediate = new ModbusTCPReadDWordIntermediate(offset, intermediate::setNumber);
                break;
            case QUAD_WORD:
                readNumberIntermediate = new ModbusTCPReadQWordIntermediate(offset, intermediate::setNumber);
                break;
            default:
                return null;
        }

        if(data.isSigned())
        {
            readNumberIntermediate.setSigned();
        }

        return readNumberIntermediate;
    }


    private ModbusTCPDataPropertyHolder.CachedData getCachedData(AddressAttribute attribute)
    {
        if (attribute == null ||
                attribute.getValue(AddressAttribute.DATA_TYPE) != AddressDataType.MODBUS_TCP)
        {
            return null;
        }

        var cachedData = ModbusTCPDataPropertyHolder.getCachedDataOf(attribute);
        if (cachedData.getOffset() < 0)
        {
            return null;
        }

        return cachedData;
    }
}
