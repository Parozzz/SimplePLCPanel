package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.doubleword;

import parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate.ModbusWriteNumberIntermediate;

public class ModbusWriteDWordIntermediate extends ModbusWriteNumberIntermediate
{
    private final int value;
    private int valueCounter;
    public ModbusWriteDWordIntermediate(int firstOffset, int value)
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
