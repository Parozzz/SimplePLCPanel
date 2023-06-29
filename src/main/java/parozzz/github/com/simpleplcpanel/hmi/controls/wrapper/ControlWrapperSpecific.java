package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper;

public interface ControlWrapperSpecific
{
    ControlWrapper<?> getSelectedControlWrapper();

    void setSelectedControlWrapper(ControlWrapper<?> controlWrapper);
}
