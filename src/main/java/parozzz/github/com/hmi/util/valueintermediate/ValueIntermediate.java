package parozzz.github.com.hmi.util.valueintermediate;

import java.util.ArrayList;
import java.util.List;

public abstract class ValueIntermediate
{
    private final List<Runnable> newValueRunnableList;

    public ValueIntermediate()
    {
        this.newValueRunnableList = new ArrayList<>();
    }

    public void addNewValueRunnable(Runnable runnable)
    {
        newValueRunnableList.add(runnable);
    }

    public void removeNewValueRunnable(Runnable runnable)
    {
        newValueRunnableList.remove(runnable);
    }

    void newValueReceived()
    {
        newValueRunnableList.forEach(Runnable::run);
    }

    public void forceNewValueReceived()
    {
        newValueReceived();
    }

    public abstract Object getObject();

    public final void setBoolean(boolean value)
    {
        this.setBoolean(value, false);
    }

    public abstract void setBoolean(boolean value, boolean forceNewValue);

    public final void setNumber(Number number)
    {
        this.setNumber(number, false);
    }

    public abstract void setNumber(Number number, boolean forceNewValue);

    public final void setString(String string)
    {
        this.setString(string, false);
    }

    public abstract void setString(String string, boolean forceNewValue);

    public abstract boolean asBoolean();

    public abstract byte asByte();

    public abstract short asShort();

    public abstract int asInteger();

    public abstract long asLong();

    public abstract float asFloat();

    public abstract double asDouble();

    public abstract String asString();

    public abstract String asStringHex();
}
