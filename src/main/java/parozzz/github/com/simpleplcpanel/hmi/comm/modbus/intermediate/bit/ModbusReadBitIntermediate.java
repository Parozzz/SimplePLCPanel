package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit;

import parozzz.github.com.simpleplcpanel.hmi.comm.ReadOnlyIntermediate;
import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusIntermediate;
import parozzz.github.com.simpleplcpanel.util.functionalinterface.primitives.BooleanConsumer;

public class ModbusReadBitIntermediate extends ModbusIntermediate implements ReadOnlyIntermediate
{
    private final BooleanConsumer consumer;
    private boolean value;
    public ModbusReadBitIntermediate(int offset, BooleanConsumer consumer)
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
