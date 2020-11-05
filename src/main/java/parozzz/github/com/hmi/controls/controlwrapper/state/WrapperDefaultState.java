package parozzz.github.com.hmi.controls.controlwrapper.state;

final class WrapperDefaultState extends WrapperState
{
    protected WrapperDefaultState()
    {
        super(Type.EQUAL, 0, 0);
    }

    public String getStringVersion()
    {
        return "DEFAULT";
    }

    @Override
    public boolean isActive(int value)
    {
        return false;
    }

    public boolean isDefault()
    {
        return true;
    }

    @Override
    public int compareTo(WrapperState wrapperState)
    {
        return -1;
    }
}
