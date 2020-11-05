package parozzz.github.com.hmi.util.valueintermediate;

import parozzz.github.com.util.Util;
import parozzz.github.com.util.number.NumberType;

import java.util.Objects;

public final class NumberIntermediate extends ValueIntermediate
{
    private Number value;

    public NumberIntermediate()
    {
        //Setting the initial value to zero
        this.value = 0;
    }

    @Override
    public Object getObject()
    {
        return value;
    }

    @Override
    public void setBoolean(boolean value, boolean forceNewValue)
    {
        this.setNumber(value ? 1 : 0, forceNewValue);
    }

    @Override
    public void setNumber(Number newValue, boolean forceNewValue)
    {
        Objects.requireNonNull(newValue, "Cannot set a null value to a ControlWrapperNumberIntermediate");
        if(!newValue.equals(value) || forceNewValue)
        {
            this.value = newValue;

            super.newValueReceived();
        }
    }

    @Override
    public void setString(String string, boolean forceNewValue)
    {
        Number newValue;

        //Try to parse as long before. If not, try double
        var parsedLong = Util.parseNumber(string, Long::parseLong, Long.MIN_VALUE);
        if(parsedLong == Long.MIN_VALUE)
        {
            var parsedDouble = Util.parseDouble(string, Double.MIN_VALUE);
            if(parsedDouble == Double.MIN_VALUE)
            {
                return;
            }

            newValue = parsedDouble;
        }
        else
        {
            newValue = parsedLong;
        }

        this.setNumber(newValue, forceNewValue);
    }

    @Override
    public boolean asBoolean()
    {
        return value.intValue() != 0;
    }

    @Override
    public byte asByte()
    {
        return value.byteValue();
    }

    @Override
    public short asShort()
    {
        return value.shortValue();
    }

    @Override
    public int asInteger()
    {
        return value.intValue();
    }

    @Override
    public long asLong()
    {
        return value.longValue();
    }

    @Override
    public float asFloat()
    {
        return value.floatValue();
    }

    @Override
    public double asDouble()
    {
        return value.doubleValue();
    }

    @Override
    public String asString()
    {
        return "" + value;
    }

    @Override
    public String asStringHex()
    {
        return "0x" + NumberType.getFromClass(value.getClass()).toHexString(value);
    }
}
