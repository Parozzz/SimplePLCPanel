package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.quadword;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusReadNumberIntermediate;

import java.util.function.LongConsumer;

public class ModbusReadQWordIntermediate extends ModbusReadNumberIntermediate
{
    private final LongConsumer consumer;
    private long value = 0;
    private int valueCounter;
    public ModbusReadQWordIntermediate(int firstOffset, LongConsumer consumer)
    {
        super(firstOffset, firstOffset + 1, firstOffset + 2, firstOffset + 3);

        this.consumer = consumer;
    }

    @Override
    public void parse()
    {
        consumer.accept(value);
    }

    @Override
    public void setNextWord(int value)
    {
        //A Quad Word is 8 bytes, so a long contains it all :)
        //Every new value added, it is added to the actual value by shifting the set value by 16 * (values received)
        this.value |= ((long) (value & 0x0000FFFF) << 16 * valueCounter++);
    }
}
