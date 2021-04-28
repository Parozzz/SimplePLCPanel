package parozzz.github.com.simpleplcpanel.hmi.comm.modbustcp.intermediate;

public abstract class ModbusTCPIntermediate
{
    private final int[] offsetArray;
    public ModbusTCPIntermediate(int... offsets)
    {
        this.offsetArray = offsets;
    }

    public int[] getOffsetArray()
    {
        return offsetArray;
    }
}
