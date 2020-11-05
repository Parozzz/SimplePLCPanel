package parozzz.github.com.hmi.comm.modbustcp;

public enum ModbusTCPFunctionCode
{
    COIL(false), //These are output bits
    DISCRETE_INPUT(true), //These are input bits
    HOLDING_REGISTER(false),
    INPUT_REGISTER(true); //There are input signal values

    private final boolean readOnly;
    ModbusTCPFunctionCode(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }
}
