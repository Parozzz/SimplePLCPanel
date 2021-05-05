package parozzz.github.com.simpleplcpanel.hmi.database;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.*;
import parozzz.github.com.simpleplcpanel.PLC.siemens.util.SiemensS7AreaType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeFetcher;
import parozzz.github.com.simpleplcpanel.hmi.attribute.AttributeType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.AddressAttribute;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.AddressDataType;
import parozzz.github.com.simpleplcpanel.hmi.attribute.impl.address.data.SiemensDataPropertyHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7ReadableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableBitWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

import java.util.Set;
import java.util.function.Consumer;

final class SiemensPLCDataUpdater extends ControlDataUpdater<SiemensS7Thread>
{
    public SiemensPLCDataUpdater(ControlContainerDatabase controlContainerDatabase, SiemensS7Thread plcThread)
    {
        super(controlContainerDatabase, plcThread);
    }

    @Override
    public void parseReadData()
    {
        var readSet = commThread.getReadDataIntermediateSet();
        if (!readSet.isEmpty())
        {
            readSet.forEach(SiemensS7ReadableWrappedDataIntermediate::parse);
        }
    }

    @Override
    public void update()
    {
        if (commThread.isUpdating())
        {
            return;
        }

        var readSet = commThread.getReadDataIntermediateSet();
        readSet.clear();

        var writeSet = commThread.getWriteDataIntermediateSet();
        writeSet.clear();

        var writeBitSet = commThread.getWriteBitWrapperSet();
        writeBitSet.clear();

        for (var controlWrapper : controlContainerDatabase.getControlWrapperSet())
        {
            if (newValueControlWrapperSet.remove(controlWrapper))
            {
                //First write then read
                var writeAddressAllData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.WRITE_ADDRESS));
                if (writeAddressAllData != null)
                {
                    var internalValue = controlWrapper.getValue().getInternalValue();
                    this.parseWrite(internalValue, writeAddressAllData, writeSet, writeBitSet);
                }
            }

            var readAddressAllData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.READ_ADDRESS));
            if (readAddressAllData != null)
            {
                var externalValue = controlWrapper.getValue().getOutsideValue();
                this.parseRead(externalValue, readAddressAllData, readSet);
            }
        }

        //There needs to be some values inside this sets, otherwise it makes no sense to commit an update
        if (!(readSet.isEmpty() && writeSet.isEmpty() && writeBitSet.isEmpty()))
        {
            commThread.doUpdate();
        }
    }

    private void parseWrite(ValueIntermediate valueIntermediate,
                            SiemensDataPropertyHolder.CachedData cachedData,
                            Set<SiemensS7WritableWrappedDataIntermediate<?>> writeSet,
                            Set<SiemensS7WritableBitWrappedDataIntermediate> writeBitSet)
    {

        var s7Data = cachedData.getS7Data();

        SiemensS7WritableWrappedDataIntermediate<?> writableWrappedDataIntermediate = null;
        if (s7Data instanceof SiemensS7BitData)
        {
            var writableBitDataIntermediate = new SiemensS7WritableBitWrappedDataIntermediate(
                    cachedData.getS7AreaType(), cachedData.getDbNumber(),
                    cachedData.getByteOffset(), cachedData.getBitOffset(),
                    valueIntermediate.asBoolean());
            writeBitSet.add(writableBitDataIntermediate);
            return;
        } else if (s7Data instanceof SiemensS7ByteData)
        {
            var byteData = (SiemensS7ByteData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, byteData, valueIntermediate.asByte());
        } else if (s7Data instanceof SiemensS7WordData)
        {
            var wordData = (SiemensS7WordData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, wordData, valueIntermediate.asInteger());
        } else if (s7Data instanceof SiemensS7ShortData)
        {
            var shortData = (SiemensS7ShortData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, shortData, valueIntermediate.asShort());
        } else if (s7Data instanceof SiemensS7DIntData)
        {
            var dIntData = (SiemensS7DIntData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, dIntData, valueIntermediate.asInteger());
        } else if (s7Data instanceof SiemensS7DWordData)
        {
            var dWordData = (SiemensS7DWordData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, dWordData, valueIntermediate.asLong());
        } else if (s7Data instanceof SiemensS7FloatData)
        {
            var floatData = (SiemensS7FloatData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, floatData, valueIntermediate.asFloat());
        } else if (s7Data instanceof SiemensS7DoubleData)
        {
            var doubleData = (SiemensS7DoubleData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, doubleData, valueIntermediate.asDouble());
        } else if (s7Data instanceof SiemensS7StringData)
        {
            var stringData = SiemensS7DataStorage.getString(cachedData.getStringLength());
            writableWrappedDataIntermediate = this.createWriteIntermediate(cachedData, stringData, valueIntermediate.asString());
        }

        if (writableWrappedDataIntermediate != null)
        {
            writeSet.add(writableWrappedDataIntermediate);
        }
    }

    private <T> SiemensS7WritableWrappedDataIntermediate<T> createWriteIntermediate(
            SiemensDataPropertyHolder.CachedData cachedData,
            SiemensS7Data<T> readableData, T value)
    {
        return new SiemensS7WritableWrappedDataIntermediate<>(readableData,
                cachedData.getS7AreaType(), cachedData.getDbNumber(), cachedData.getByteOffset(), value);
    }

    private void parseRead(ValueIntermediate valueIntermediate,
                           SiemensDataPropertyHolder.CachedData cachedData,
                           Set<SiemensS7ReadableWrappedDataIntermediate<?>> readSet)
    {
        var s7Data = cachedData.getS7Data();

        SiemensS7ReadableWrappedDataIntermediate<?> readableWrappedDataIntermediate = null;
        if (s7Data instanceof SiemensS7BitData)
        {
            var bitData = SiemensS7DataStorage.getBit(cachedData.getBitOffset());
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, bitData, valueIntermediate::setBoolean);
        } else if (s7Data instanceof SiemensS7ByteData)
        {
            var byteData = (SiemensS7ByteData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, byteData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7WordData)
        {
            var wordData = (SiemensS7WordData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, wordData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7ShortData)
        {
            var shortData = (SiemensS7ShortData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, shortData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DIntData)
        {
            var dIntData = (SiemensS7DIntData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, dIntData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DWordData)
        {
            var dWordData = (SiemensS7DWordData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, dWordData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7FloatData)
        {
            var floatData = (SiemensS7FloatData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, floatData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DoubleData)
        {
            var doubleData = (SiemensS7DoubleData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, doubleData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7StringData)
        {
            var stringData = SiemensS7DataStorage.getString(cachedData.getStringLength());
            readableWrappedDataIntermediate = this.createReadIntermediate(cachedData, stringData, valueIntermediate::setString);
        }

        if (readableWrappedDataIntermediate != null)
        {
            readSet.add(readableWrappedDataIntermediate);
        }
    }

    private <T> SiemensS7ReadableWrappedDataIntermediate<T> createReadIntermediate(
            SiemensDataPropertyHolder.CachedData cachedData,
            SiemensS7ReadableData<T> readableData, Consumer<T> consumer)
    {
        return new SiemensS7ReadableWrappedDataIntermediate<>(readableData,
                cachedData.getS7AreaType(), cachedData.getDbNumber(), cachedData.getByteOffset(), consumer);
    }

    private SiemensDataPropertyHolder.CachedData getAddressData(AddressAttribute addressAttribute)
    {
        if (addressAttribute == null ||
                addressAttribute.getValue(AddressAttribute.DATA_TYPE) != AddressDataType.SIEMENS)
        {
            return null;
        }

        var allData = SiemensDataPropertyHolder.getCachedDataOf(addressAttribute);
        if (allData.getS7AreaType() == null
                || (allData.getS7AreaType() == SiemensS7AreaType.DB && allData.getDbNumber() <= 0)
                || allData.getS7Data() == null)
        {
            return null;
        }

        return allData;
    }

}
