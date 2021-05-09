package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate;

public abstract class ModbusWriteNumberIntermediate extends ModbusIntermediate
{
    public ModbusWriteNumberIntermediate(int... offsets)
    {
        super(offsets);
    }

    public abstract int getNextWord();
}
