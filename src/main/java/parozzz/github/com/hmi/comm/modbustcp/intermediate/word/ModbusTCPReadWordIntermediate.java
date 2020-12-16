package parozzz.github.com.hmi.comm.modbustcp.intermediate.word;

import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPReadNumberIntermediate;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

public class ModbusTCPReadWordIntermediate extends ModbusTCPReadNumberIntermediate
{
    private final IntConsumer consumer;
    private int value;

    public ModbusTCPReadWordIntermediate(int offset, IntConsumer consumer)
    {
        super(offset);
        this.consumer = consumer;
    }

    @Override
    public void setNextWord(int value)
    {
        this.value = super.signed
                ? ByteBuffer.allocate(4).putInt(value).getShort(2)
                : value;
    }

    @Override
    public void parse()
    {
        consumer.accept(value);
    }


}
