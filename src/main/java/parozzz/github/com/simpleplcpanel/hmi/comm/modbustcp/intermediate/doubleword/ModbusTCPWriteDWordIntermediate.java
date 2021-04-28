package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.doubleword;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate.ModbusTCPWriteNumberIntermediate;

public class ModbusTCPWriteDWordIntermediate extends ModbusTCPWriteNumberIntermediate
{
    private final int value;
    private int valueCounter;
    public ModbusTCPWriteDWordIntermediate(int firstOffset, int value)
    {
        super(firstOffset, firstOffset + 1);

        this.value = value;
    }

    @Override
    public int getNextWord()
    {
        return (value >> (valueCounter++ * 16) & 0xFFFF);
    }
}
