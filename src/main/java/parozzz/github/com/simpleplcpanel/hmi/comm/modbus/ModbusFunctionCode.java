package parozzz.github.com.simpleplcpanel.hmi.comm.modbus;

public enum ModbusFunctionCode
{
    COIL(false), //These are output bits
    DISCRETE_INPUT(true), //These are input bits
    INPUT_REGISTER(true), //There are input signal values
    HOLDING_REGISTER(false);


    private final boolean readOnly;
    ModbusFunctionCode(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }
}
