package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.state;

final class WrapperDefaultState extends WrapperState
{
    protected WrapperDefaultState(WrapperStateMap wrapperStateMap)
    {
        super(wrapperStateMap, 0, CompareType.EQUAL, 0, CompareType.EQUAL);
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
