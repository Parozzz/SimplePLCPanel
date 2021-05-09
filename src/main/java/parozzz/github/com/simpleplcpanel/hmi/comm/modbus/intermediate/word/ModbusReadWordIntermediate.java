package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.word;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusReadNumberIntermediate;

import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

public class ModbusReadWordIntermediate extends ModbusReadNumberIntermediate
{
    private final IntConsumer consumer;
    private int value;

    public ModbusReadWordIntermediate(int offset, IntConsumer consumer)
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
