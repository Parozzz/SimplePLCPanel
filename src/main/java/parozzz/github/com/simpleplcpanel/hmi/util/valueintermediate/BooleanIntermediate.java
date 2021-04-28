package parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate;

public final class BooleanIntermediate extends ValueIntermediate
{
    private boolean value;

    public BooleanIntermediate()
    {
        this.value = false;
    }

    @Override
    public Object getObject()
    {
        return value;
    }

    @Override
    public void setBoolean(boolean newValue, boolean forceNewValue)
    {
        if(this.value != newValue || forceNewValue)
        {
            this.value = newValue;
            super.newValueReceived();
        }
    }

    @Override
    public void setNumber(Number number, boolean forceNewValue)
    {
        this.setBoolean(number.intValue() != 0, forceNewValue);
    }

    @Override
    public void setString(String string, boolean forceNewValue)
    {
        this.setBoolean(Boolean.parseBoolean(string), forceNewValue);
    }

    private void setValue(boolean newValue, boolean forceNewValue)
    {
        if(this.value != newValue || forceNewValue)
        {
            this.value = newValue;

            super.newValueReceived();
        }
    }

    @Override
    public boolean asBoolean()
    {
        return value;
    }

    @Override
    public byte asByte()
    {
        return asBoolean() ? (byte) 1 : 0;
    }

    @Override
    public short asShort()
    {
        return asBoolean() ? (short) 1 : 0;
    }

    @Override
    public int asInteger()
    {
        return asBoolean() ? 1 : 0;
    }

    @Override
    public long asLong()
    {
        return asBoolean() ? 1L : 0;
    }

    @Override
    public float asFloat()
    {
        return asBoolean() ? 1F : 0;
    }

    @Override
    public double asDouble()
    {
        return asBoolean() ? 1D : 0;
    }

    @Override
    public String asString()
    {
        return asBoolean() ? "TRUE" : "FALSE";
    }

    @Override
    public String asStringHex()
    {
        return asBoolean() ? "0x1" : "0x0";
    }
}
