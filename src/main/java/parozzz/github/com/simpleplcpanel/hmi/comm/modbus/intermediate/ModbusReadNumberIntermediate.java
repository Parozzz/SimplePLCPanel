package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate;

import parozzz.github.com.simpleplcpanel.hmi.comm.ReadOnlyIntermediate;

public abstract class ModbusReadNumberIntermediate
        extends ModbusIntermediate
        implements ReadOnlyIntermediate
{
    protected boolean signed;
    public ModbusReadNumberIntermediate(int... offsets)
    {
        super(offsets);
    }

    public final void setSigned()
    {
        signed = true;
    }

    public abstract void setNextWord(int value);
}
