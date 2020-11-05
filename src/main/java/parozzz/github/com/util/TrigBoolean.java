package parozzz.github.com.util;

public final class TrigBoolean
{
    public enum TrigType
    {
        NONE,
        RISING,
        FALLING;
    }

    private boolean state;
    private boolean oldState;

    public TrigBoolean(boolean startState)
    {
        this.state = startState;
        this.oldState = startState;
    }

    public boolean get()
    {
        return state;
    }

    public void set(boolean state)
    {
        this.state = state;
    }

    public TrigType checkTrig()
    {
        var trigType = TrigType.NONE;
        if(rising())
        {
            trigType = TrigType.RISING;
        }
        else if(falling())
        {
            trigType = TrigType.FALLING;
        }
        updateOld();
        return trigType;
    }

    private boolean rising()
    {
        return state && !oldState;
    }

    private boolean falling()
    {
        return !state && oldState;
    }

    public void updateOld()
    {
        this.oldState = this.state;
    }
}
