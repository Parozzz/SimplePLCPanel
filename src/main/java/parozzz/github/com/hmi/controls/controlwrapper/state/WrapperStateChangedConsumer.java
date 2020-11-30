package parozzz.github.com.hmi.controls.controlwrapper.state;

@FunctionalInterface
public interface WrapperStateChangedConsumer
{
    void stateChanged(WrapperState newState, WrapperState oldState, int state);
}
