package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.word;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusWriteNumberIntermediate;

public class ModbusWriteWordIntermediate extends ModbusWriteNumberIntermediate
{
    private final int value;
    public ModbusWriteWordIntermediate(int offset, int value)
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
