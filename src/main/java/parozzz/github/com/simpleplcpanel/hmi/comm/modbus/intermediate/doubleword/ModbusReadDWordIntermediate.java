package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.doubleword;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusReadNumberIntermediate;

import java.nio.ByteBuffer;
import java.util.function.LongConsumer;

public class ModbusReadDWordIntermediate extends ModbusReadNumberIntermediate
{
    private final LongConsumer consumer;
    private long value = 0;
    private int valueCounter;
    public ModbusReadDWordIntermediate(int firstOffset, LongConsumer consumer)
    {
        super(firstOffset, firstOffset + 1);

        this.consumer = consumer;
    }

    @Override
    public void setNextWord(int value)
    {
        //A Double Word is 4 bytes, so an integer contains it all :)
        //Every new value added, it is added to the actual value by shifting the set value by 16 * (values received)
        this.value |= (long) (value & 0xFFFF) << (16 * valueCounter ++);

        if(signed && valueCounter == 2)
        {
            this.value = ByteBuffer.allocate(8).putLong(this.value).getInt(4);
        }
    }

    @Override
    public void parse()
    {
        consumer.accept(value);
    }
}
