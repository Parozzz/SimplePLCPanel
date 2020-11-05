package parozzz.github.com.hmi.comm.modbustcp.intermediate.quadword;

import parozzz.github.com.hmi.comm.modbustcp.intermediate.ModbusTCPWriteNumberIntermediate;

public class ModbusTCPWriteQWordIntermediate extends ModbusTCPWriteNumberIntermediate
{
    private final long value;
    private int valueCounter;
    public ModbusTCPWriteQWordIntermediate(int firstOffset, long value)
    {
        super(firstOffset, firstOffset + 1, firstOffset + 2, firstOffset + 3);

        this.value = value;
    }

    @Override
    public int getNextWord()
    {
        return (int) ((value >> (16 * valueCounter ++)) & 0xFFFF);
    }
}
