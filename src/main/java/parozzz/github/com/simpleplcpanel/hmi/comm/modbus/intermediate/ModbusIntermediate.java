package parozzz.github.com.simpleplcpanel.hmi.comm.modbus.intermediate;

public abstract class ModbusIntermediate
{
    private final int[] offsetArray;
    public ModbusIntermediate(int... offsets)
    {
        this.offsetArray = offsets;
    }

    public int[] getOffsetArray()
    {
        return offsetArray;
    }
}
