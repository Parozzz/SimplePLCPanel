package parozzz.github.com.simpleplcpanel.hmi.tags.updater;

import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7Data;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7DataStorage;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.SiemensS7ReadableData;
import parozzz.github.com.simpleplcpanel.PLC.siemens.data.primitives.*;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.SiemensS7Thread;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7ReadableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableBitWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.intermediate.SiemensS7WritableWrappedDataIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.siemens.stringaddress.SiemensS7StringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.tags.TagsManager;
import parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate.ValueIntermediate;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class SiemensPLCValueUpdater extends TagValueUpdater<SiemensS7Thread>
{
    public static SiemensPLCValueUpdater createInstance(TagsManager tagsManager,
            CommunicationDataHolder communicationDataHolder)
    {
        var siemensS7Thread = communicationDataHolder.getCommThread(CommunicationType.SIEMENS_S7, SiemensS7Thread.class);
        Objects.requireNonNull(siemensS7Thread, "SiemensS7Thread is null while creating SiemensPLCDataUpdater?");

        return new SiemensPLCValueUpdater(tagsManager, communicationDataHolder, siemensS7Thread);
    }

    public SiemensPLCValueUpdater(TagsManager tagsManager,
            CommunicationDataHolder communicationDataHolder,
            SiemensS7Thread siemensS7Thread)
    {
        super(tagsManager, CommunicationType.SIEMENS_S7, communicationDataHolder, siemensS7Thread);
    }

    @Override
    public void parseReadData()
    {
        var readSet = communicationThread.getReadDataIntermediateSet();
        if (!readSet.isEmpty())
        {
            readSet.forEach(SiemensS7ReadableWrappedDataIntermediate::parse);
        }
    }

    @Override
    public void update()
    {
        if (communicationThread.isUpdating())
        {
            return;
        }

        var readSet = communicationThread.getReadDataIntermediateSet();
        readSet.clear();

        var writeSet = communicationThread.getWriteDataIntermediateSet();
        writeSet.clear();

        var writeBitSet = communicationThread.getWriteBitWrapperSet();
        writeBitSet.clear();

        for (var tag : tagsManager)
        {
            if (!(tag instanceof CommunicationTag))
            {
                continue;
            }

            var commTag = (CommunicationTag) tag;

            var tagActive = commTag.hasProperty(CommunicationTag.TagProperty.ACTIVE);
            var stringAddressData = commTag.getStringAddressData();
            if (commTag.isLocal() || !tagActive
                    || !(stringAddressData instanceof SiemensS7StringAddressData))
            {
                continue;
            }

            var siemensS7StringAddressData = (SiemensS7StringAddressData) stringAddressData;

            if (super.needWriteTagSet.remove(tag))
            {
                var writeIntermediate = commTag.getWriteIntermediate();
                this.parseWrite(writeIntermediate, siemensS7StringAddressData, writeSet, writeBitSet);
            }

            if (commTag.hasProperty(CommunicationTag.TagProperty.NEED_READ))
            {
                var readIntermediate = commTag.getReadIntermediate();
                this.parseRead(readIntermediate, siemensS7StringAddressData, readSet);
            }
        }
        /*
        for (var controlWrapper : controlContainerDatabase.getControlWrapperSet())
        {
            if (newValueControlWrapperSet.remove(controlWrapper))
            {
                //First write then read
                var writeAddressData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.WRITE_ADDRESS));
                if (writeAddressData != null)
                {
                    var internalValue = controlWrapper.getValue().getInternalValue();
                    this.parseWrite(internalValue, writeAddressData, writeSet, writeBitSet);
                }
            }

            var readAddressData = this.getAddressData(AttributeFetcher.fetch(controlWrapper, AttributeType.READ_ADDRESS));
            if (readAddressData != null)
            {
                var externalValue = controlWrapper.getValue().getOutsideValue();
                this.parseRead(externalValue, readAddressData, readSet);
            }
        }*/

        //There needs to be some values inside this sets, otherwise it makes no sense to commit an update
        if (!(readSet.isEmpty() && writeSet.isEmpty() && writeBitSet.isEmpty()))
        {
            communicationThread.doUpdate();
        }
    }

    private void parseWrite(ValueIntermediate valueIntermediate,
            SiemensS7StringAddressData addressData,
            Set<SiemensS7WritableWrappedDataIntermediate<?>> writeSet,
            Set<SiemensS7WritableBitWrappedDataIntermediate> writeBitSet)
    {

        var s7Data = addressData.getReadableData();

        SiemensS7WritableWrappedDataIntermediate<?> writableWrappedDataIntermediate = null;
        if (s7Data instanceof SiemensS7BitData)
        {
            var writableBitDataIntermediate = new SiemensS7WritableBitWrappedDataIntermediate(
                    addressData.getAreaType(), addressData.getDbNumber(),
                    addressData.getByteOffset(), addressData.getBitOffset(),
                    valueIntermediate.asBoolean());
            writeBitSet.add(writableBitDataIntermediate);
            return;
        } else if (s7Data instanceof SiemensS7ByteData)
        {
            var byteData = (SiemensS7ByteData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, byteData, valueIntermediate.asByte());
        } else if (s7Data instanceof SiemensS7WordData)
        {
            var wordData = (SiemensS7WordData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, wordData, valueIntermediate.asInteger());
        } else if (s7Data instanceof SiemensS7ShortData)
        {
            var shortData = (SiemensS7ShortData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, shortData, valueIntermediate.asShort());
        } else if (s7Data instanceof SiemensS7DIntData)
        {
            var dIntData = (SiemensS7DIntData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, dIntData, valueIntermediate.asInteger());
        } else if (s7Data instanceof SiemensS7DWordData)
        {
            var dWordData = (SiemensS7DWordData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, dWordData, valueIntermediate.asLong());
        } else if (s7Data instanceof SiemensS7FloatData)
        {
            var floatData = (SiemensS7FloatData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, floatData, valueIntermediate.asFloat());
        } else if (s7Data instanceof SiemensS7DoubleData)
        {
            var doubleData = (SiemensS7DoubleData) s7Data;
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, doubleData, valueIntermediate.asDouble());
        } else if (s7Data instanceof SiemensS7StringData)
        {
            var stringData = SiemensS7DataStorage.getString(addressData.getStringLength());
            writableWrappedDataIntermediate = this.createWriteIntermediate(addressData, stringData, valueIntermediate.asString());
        }

        if (writableWrappedDataIntermediate != null)
        {
            writeSet.add(writableWrappedDataIntermediate);
        }
    }

    private <T> SiemensS7WritableWrappedDataIntermediate<T> createWriteIntermediate(
            SiemensS7StringAddressData addressData,
            SiemensS7Data<T> readableData, T value)
    {
        return new SiemensS7WritableWrappedDataIntermediate<>(readableData,
                addressData.getAreaType(), addressData.getDbNumber(), addressData.getByteOffset(), value);
    }

    private void parseRead(ValueIntermediate valueIntermediate,
            SiemensS7StringAddressData addressData,
            Set<SiemensS7ReadableWrappedDataIntermediate<?>> readSet)
    {
        var s7Data = addressData.getReadableData();

        SiemensS7ReadableWrappedDataIntermediate<?> readableWrappedDataIntermediate = null;
        if (s7Data instanceof SiemensS7BitData)
        {
            var bitData = SiemensS7DataStorage.getBit(addressData.getBitOffset());
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, bitData, valueIntermediate::setBoolean);
        } else if (s7Data instanceof SiemensS7ByteData)
        {
            var byteData = (SiemensS7ByteData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, byteData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7WordData)
        {
            var wordData = (SiemensS7WordData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, wordData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7ShortData)
        {
            var shortData = (SiemensS7ShortData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, shortData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DIntData)
        {
            var dIntData = (SiemensS7DIntData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, dIntData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DWordData)
        {
            var dWordData = (SiemensS7DWordData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, dWordData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7FloatData)
        {
            var floatData = (SiemensS7FloatData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, floatData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7DoubleData)
        {
            var doubleData = (SiemensS7DoubleData) s7Data;
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, doubleData, valueIntermediate::setNumber);
        } else if (s7Data instanceof SiemensS7StringData)
        {
            var stringData = SiemensS7DataStorage.getString(addressData.getStringLength());
            readableWrappedDataIntermediate = this.createReadIntermediate(addressData, stringData, valueIntermediate::setString);
        }

        if (readableWrappedDataIntermediate != null)
        {
            readSet.add(readableWrappedDataIntermediate);
        }
    }

    private <T> SiemensS7ReadableWrappedDataIntermediate<T> createReadIntermediate(
            SiemensS7StringAddressData addressData,
            SiemensS7ReadableData<T> readableData, Consumer<T> consumer)
    {
        return new SiemensS7ReadableWrappedDataIntermediate<>(readableData,
                addressData.getAreaType(), addressData.getDbNumber(), addressData.getByteOffset(), consumer);
    }
}
