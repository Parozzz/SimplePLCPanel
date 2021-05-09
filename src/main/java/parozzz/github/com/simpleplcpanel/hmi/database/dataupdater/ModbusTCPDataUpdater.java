package parozzz.github.com.simpleplcpanel.hmi.database.dataupdater;

import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.propertyholders.ModbusAttributePropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.ReadOnlyIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.stringaddress.ModbusStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.tcp.ModbusTCPThread;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusReadNumberIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit.ModbusReadBitIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit.ModbusWriteBitIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.doubleword.ModbusReadDWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.doubleword.ModbusWriteDWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.quadword.ModbusReadQWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.quadword.ModbusWriteQWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.word.ModbusReadWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.word.ModbusWriteWordIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.setup.impl.AddressSetupPane;
import parozzz.github.com.simpleplcpanel.hmi.database.ControlContainerDatabase;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

import java.util.Objects;
import java.util.Set;

public final class ModbusTCPDataUpdater extends ControlDataUpdater<ModbusTCPThread>
{
    public static ModbusTCPDataUpdater createInstance(ControlContainerDatabase controlContainerDatabase,
            CommunicationDataHolder communicationDataHolder)
    {
        var modbusTCPThread = communicationDataHolder.getCommThread(CommunicationType.MODBUS_TCP, ModbusTCPThread.class);
        Objects.requireNonNull(modbusTCPThread, "ModbusTCPThread is null while creating ModbusTCPDataUpdater?");

        return new ModbusTCPDataUpdater(controlContainerDatabase, communicationDataHolder, modbusTCPThread);
    }

    private ModbusTCPDataUpdater(ControlContainerDatabase controlContainerDatabase,
            CommunicationDataHolder communicationDataHolder, ModbusTCPThread modbusTCPThread)
    {
        super(CommunicationType.MODBUS_TCP, controlContainerDatabase,
                communicationDataHolder,modbusTCPThread);
    }

    @Override
    public void parseReadData()
    {
        this.parseReadOnlySet(commThread.getReadCoilSet());
        this.parseReadOnlySet(commThread.getReadDiscreteInputsSet());
        this.parseReadOnlySet(commThread.getReadHoldingRegisterSet());
        this.parseReadOnlySet(commThread.getReadInputRegistersSet());
    }

    private void parseReadOnlySet(Set<? extends ReadOnlyIntermediate> intermediateSet)
    {
        if(intermediateSet != null && !intermediateSet.isEmpty())
        {
            intermediateSet.forEach(ReadOnlyIntermediate::parse);
        }
    }

    @Override
    public void update()
    {
        if(commThread.isUpdating())
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

        for(var controlWrapper : controlContainerDatabase.getControlWrapperSet())
        {
            if(newValueControlWrapperSet.remove(controlWrapper))
            {
                var writeAddressData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.WRITE_ADDRESS));
                if(writeAddressData != null)
                {
                    var offset = writeAddressData.getOffset();
                    var internalValue = controlWrapper.getValue().getInternalValue();
                    switch(writeAddressData.getFunctionCode())
                    {
                        case HOLDING_REGISTER:
                            switch(writeAddressData.getDataLength())
                            {
                                case WORD:
                                    writeHoldingRegisterSet.add(new ModbusWriteWordIntermediate(offset, internalValue.asInteger()));
                                    break;
                                case DOUBLE_WORD:
                                    writeHoldingRegisterSet.add(new ModbusWriteDWordIntermediate(offset, internalValue.asInteger()));
                                    break;
                                case QUAD_WORD:
                                    writeHoldingRegisterSet.add(new ModbusWriteQWordIntermediate(offset, internalValue.asLong()));
                                    break;
                            }
                            break;
                        case COIL:
                            writeCoilsSet.add(new ModbusWriteBitIntermediate(offset, internalValue.asBoolean()));
                            break;
                    }
                }

                //Parse write values here!
            }

            var readAddressData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.READ_ADDRESS));
            if(readAddressData != null)
            {
                var offset = readAddressData.getOffset();
                var outsideValue = controlWrapper.getValue().getOutsideValue();
                switch(readAddressData.getFunctionCode())
                {
                    case HOLDING_REGISTER:
                        readHoldingRegisterSet.add(this.parseReadNumberIntermediate(readAddressData, offset, outsideValue));
                        break;
                    case COIL:
                        readCoilsSet.add(new ModbusReadBitIntermediate(offset, outsideValue::setBoolean));
                        break;
                    case INPUT_REGISTER:
                        readInputRegistersSet.add(this.parseReadNumberIntermediate(readAddressData, offset, outsideValue));
                        break;
                    case DISCRETE_INPUT:
                        readDiscreteInputsSet.add(new ModbusReadBitIntermediate(offset, outsideValue::setBoolean));
                        break;
                }
            }
            //Parse read values here
        }

        if(!(readHoldingRegisterSet.isEmpty() && writeHoldingRegisterSet.isEmpty()
                && writeCoilsSet.isEmpty() && readCoilsSet.isEmpty()
                && readDiscreteInputsSet.isEmpty()
                && readInputRegistersSet.isEmpty()))
        {
            commThread.doUpdate();
        }
    }

    private ModbusReadNumberIntermediate parseReadNumberIntermediate(ModbusStringAddressData addressData,
            int offset, ValueIntermediate intermediate)
    {
        ModbusReadNumberIntermediate readNumberIntermediate;
        switch(addressData.getDataLength())
        {
            case BIT:
                var bitOffset = addressData.getBitOffset();
                readNumberIntermediate = new ModbusReadWordIntermediate(offset, value ->
                {
                    var booleanValue = (value & (1 >> bitOffset)) != 0;
                    intermediate.setBoolean(booleanValue);
                });
                break;
            case WORD:
                readNumberIntermediate = new ModbusReadWordIntermediate(offset, intermediate::setNumber);
                break;
            case DOUBLE_WORD:
                readNumberIntermediate = new ModbusReadDWordIntermediate(offset, intermediate::setNumber);
                break;
            case QUAD_WORD:
                readNumberIntermediate = new ModbusReadQWordIntermediate(offset, intermediate::setNumber);
                break;
            default:
                return null;
        }

        if(addressData.isSigned())
        {
            readNumberIntermediate.setSigned();
        }

        return readNumberIntermediate;
    }


    private ModbusStringAddressData getAddressData(AddressAttribute addressAttribute)
    {
        if(addressAttribute == null)
        {
            return null;
        }

        var addressType = addressAttribute.getValue(AddressAttribute.ADDRESS_TYPE);
        if(addressType != AddressSetupPane.AddressType.COMMUNICATION)
        {
            return null;
        }

        return addressAttribute.getValue(AddressAttribute.MODBUS_TCP_STRING_DATA);
    }
}
