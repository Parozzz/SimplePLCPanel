package parozzz.github.com.hmi.comm.modbustcp.intermediate.bit;

import parozzz.github.com.hmi.comm.ReadOnlyIntermediate;
import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPIntermediate;
import parozzz.github.com.util.functionalinterface.primitives.BooleanConsumer;

public class ModbusTCPReadBitIntermediate extends ModbusTCPIntermediate implements ReadOnlyIntermediate
{
    private final BooleanConsumer consumer;
    private boolean value;
    public ModbusTCPReadBitIntermediate(int offset, BooleanConsumer consumer)
    {
        super(offset);
        this.consumer = consumer;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }

    @Override
    public void parse()
    {
        consumer.accept(value);
    }
}
