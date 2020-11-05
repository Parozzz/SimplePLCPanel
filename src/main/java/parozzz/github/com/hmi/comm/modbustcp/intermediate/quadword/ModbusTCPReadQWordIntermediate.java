package parozzz.github.com.hmi.comm.modbustcp.intermediate.quadword;

import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPReadNumberIntermediate;

import java.util.function.LongConsumer;

public class ModbusTCPReadQWordIntermediate extends ModbusTCPReadNumberIntermediate
{
    private final LongConsumer consumer;
    private long value = 0;
    private int valueCounter;
    public ModbusTCPReadQWordIntermediate(int firstOffset, LongConsumer consumer)
    {
        super(firstOffset, firstOffset + 1, firstOffset + 2, firstOffset + 3);

        this.consumer = consumer;
    }

    public void setValue(int value)
    {   //A Double Word is 8 bytes, so a long contains it all :)
        //Every new value added, it is added to the actual value by shifting the set value by 16 * (values received)
        this.value |= (value & 0xFFFF) << (16 * valueCounter ++);
    }

    @Override
    public void parse()
    {
        consumer.accept(value);
    }

    @Override
    public void setNextWord(int value)
    {
        //A Double Word is 8 bytes, so a long contains it all :)
        //Every new value added, it is added to the actual value by shifting the set value by 16 * (values received)
        this.value &= (value & 0xFFFF) << (16 * valueCounter ++);
    }
}
