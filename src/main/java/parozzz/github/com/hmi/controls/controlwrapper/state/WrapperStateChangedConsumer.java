package parozzz.github.com.hmi.controls.controlwrapper.state;

@FunctionalInterface
public interface WrapperStateChangedConsumer
{
    enum ChangeType
    {
        ADD,
        REMOVE,
        STATE_CHANGED;
    }

    void stateChanged(WrapperStateMap stateMap, WrapperState oldState, ChangeType changeType);
}
