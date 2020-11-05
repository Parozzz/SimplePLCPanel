package parozzz.github.com.hmi.comm.modbustcp.intermediate;

import parozzz.github.com.hmi.comm.ReadOnlyIntermediate;

public abstract class ModbusTCPReadNumberIntermediate
        extends ModbusTCPIntermediate
        implements ReadOnlyIntermediate
{
    protected boolean signed;
    public ModbusTCPReadNumberIntermediate(int... offsets)
    {
        super(offsets);
    }

    public final void setSigned()
    {
        signed = true;
    }

    public abstract void setNextWord(int value);
}
