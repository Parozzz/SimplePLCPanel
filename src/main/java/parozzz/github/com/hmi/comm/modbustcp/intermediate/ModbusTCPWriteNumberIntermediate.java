package parozzz.github.com.hmi.comm.modbustcp.intermediate;

public abstract class ModbusTCPWriteNumberIntermediate extends ModbusTCPIntermediate
{
    public ModbusTCPWriteNumberIntermediate(int... offsets)
    {
        super(offsets);
    }

    public abstract int getNextWord();
}