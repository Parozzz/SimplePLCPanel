package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.quadword;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusWriteNumberIntermediate;

public class ModbusWriteQWordIntermediate extends ModbusWriteNumberIntermediate
{
    private final long value;
    private int valueCounter;
    public ModbusWriteQWordIntermediate(int firstOffset, long value)
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
