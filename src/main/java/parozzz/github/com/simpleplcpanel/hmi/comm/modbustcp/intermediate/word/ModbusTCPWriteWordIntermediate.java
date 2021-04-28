package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.word;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.ModbusTCPWriteNumberIntermediate;

public class ModbusTCPWriteWordIntermediate extends ModbusTCPWriteNumberIntermediate
{
    private final int value;
    public ModbusTCPWriteWordIntermediate(int offset, int value)
    {
        super(offset);
        this.value = value;
    }

    @Override
    public int getNextWord()
    {
        return value;
    }
}
