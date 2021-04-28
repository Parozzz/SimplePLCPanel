package parozzz.github.com.simpleplcpanel.hmi.util.valueintermediate;

import parozzz.github.com.simpleplcpanel.util.Util;

import java.util.Objects;
import java.util.stream.Collectors;

public final class StringIntermediate extends ValueIntermediate
{
    private String value;

    public StringIntermediate()
    {
        this.value = "";
    }

    @Override
    public Object getObject()
    {
        return value;
    }

    @Override
    public void setBoolean(boolean value, boolean forceNewValue)
    {
        this.setString(value ? "TRUE" : "FALSE", forceNewValue);
    }

    @Override
    public void setNumber(Number number, boolean forceNewValue)
    {
        this.setString("" + number, forceNewValue);
    }

    @Override
    public void setString(String newValue, boolean forceNewValue)
    {
        Objects.requireNonNull(newValue, "Cannot set a null value to a StringIntermediate");
        if(!newValue.equals(value) || forceNewValue)
        {
            this.value = newValue;

            super.newValueReceived();
        }
    }

    @Override
    public boolean asBoolean()
    {
        return Boolean.parseBoolean(value);
    }

    @Override
    public byte asByte()
    {
        return Util.parseNumber(value, Byte::parseByte, (byte) 0);
    }

    @Override
    public short asShort()
    {
        return Util.parseNumber(value, Short::parseShort, (short) 0);
    }

    @Override
    public int asInteger()
    {
        return Util.parseNumber(value, Integer::parseInt, 0);
    }

    @Override
    public long asLong()
    {
        return Util.parseNumber(value, Long::parseLong, 0L);
    }

    @Override
    public float asFloat()
    {
        return Util.parseNumber(value, Float::parseFloat, 0F);
    }

    @Override
    public double asDouble()
    {
        return Util.parseNumber(value, Double::parseDouble, 0d);
    }

    @Override
    public String asString()
    {
        return value;
    }

    @Override
    public String asStringHex()
    {
        return "0x" + value.chars().mapToObj(tChar -> Integer.toString(tChar, 16)).collect(Collectors.joining());
    }
}
