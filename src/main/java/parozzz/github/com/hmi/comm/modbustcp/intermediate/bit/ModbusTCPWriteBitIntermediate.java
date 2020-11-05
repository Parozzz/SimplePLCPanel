package parozzz.github.com.hmi.comm.modbustcp.intermediate.bit;

import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPIntermediate;

public class ModbusTCPWriteBitIntermediate extends ModbusTCPIntermediate
{
    private final boolean value;
    public ModbusTCPWriteBitIntermediate(int offset, boolean value)
    {
        super(offset);
        this.value = value;
    }

    public boolean getValue()
    {
        return value;
    }
}
