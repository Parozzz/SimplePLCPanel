package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.bit;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusIntermediate;

public class ModbusWriteBitIntermediate extends ModbusIntermediate
{
    private final boolean value;
    public ModbusWriteBitIntermediate(int offset, boolean value)
    {
        super(offset);
        this.value = value;
    }

    public boolean getValue()
    {
        return value;
    }
}
