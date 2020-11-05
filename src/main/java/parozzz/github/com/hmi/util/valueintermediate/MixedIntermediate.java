package parozzz.github.com.hmi.util.valueintermediate;

public final class MixedIntermediate extends ValueIntermediate
{
    private final BooleanIntermediate booleanIntermediate;
    private final StringIntermediate stringIntermediate;
    private final NumberIntermediate numberIntermediate;

    private Object lastValue;
    private ValueIntermediate lastIntermediate;

    public MixedIntermediate()
    {
        this.booleanIntermediate = new BooleanIntermediate();
        this.stringIntermediate = new StringIntermediate();
        this.numberIntermediate = new NumberIntermediate();

        this.lastValue = booleanIntermediate.asBoolean();
        //Initialize as boolean just to avoid null check later :P
        this.lastIntermediate = booleanIntermediate;
    }

    @Override
    public Object getObject()
    {
        return lastIntermediate.getObject();
    }

    @Override
    public void setBoolean(boolean value, boolean forceNewValue)
    {
        booleanIntermediate.setBoolean(value);
        this.checkChanged(booleanIntermediate, forceNewValue);
    }

    @Override
    public void setNumber(Number value, boolean forceNewValue)
    {
        numberIntermediate.setNumber(value);
        this.checkChanged(numberIntermediate, forceNewValue);
    }

    @Override
    public void setString(String value, boolean forceNewValue)
    {
        stringIntermediate.setString(value);
        this.checkChanged(stringIntermediate, forceNewValue);
    }

    private void checkChanged(ValueIntermediate newValueIntermediate, boolean forceNewValue)
    {
        if (!newValueIntermediate.equals(lastIntermediate)
                || !newValueIntermediate.getObject().equals(lastValue)
                || forceNewValue)
        {
            lastValue = newValueIntermediate.getObject();
            lastIntermediate = newValueIntermediate;

            super.newValueReceived();
        }
    }

    @Override
    public boolean asBoolean()
    {
        return lastIntermediate.asBoolean();
    }

    @Override
    public byte asByte()
    {
        return lastIntermediate.asByte();
    }

    @Override
    public short asShort()
    {
        return lastIntermediate.asShort();
    }

    @Override
    public int asInteger()
    {
        return lastIntermediate.asInteger();
    }

    @Override
    public long asLong()
    {
        return lastIntermediate.asLong();
    }

    @Override
    public float asFloat()
    {
        return lastIntermediate.asFloat();
    }

    @Override
    public double asDouble()
    {
        return lastIntermediate.asDouble();
    }

    @Override
    public String asString()
    {
        return lastIntermediate.asString();
    }

    @Override
    public String asStringHex()
    {
        return lastIntermediate.asStringHex();
    }
}
