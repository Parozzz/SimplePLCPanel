package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper;

public interface ControlWrapperSpecific
{
    ControlWrapper<?> getSelectedControlWrapper();

    void setSelectedControlWrapper(ControlWrapper<?> controlWrapper);
}
