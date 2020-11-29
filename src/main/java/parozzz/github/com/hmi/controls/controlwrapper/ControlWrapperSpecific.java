package parozzz.github.com.hmi.controls.controlwrapper;

public interface ControlWrapperSpecific
{
    ControlWrapper<?> getSelectedControlWrapper();

    void setSelectedControlWrapper(ControlWrapper<?> controlWrapper);
}
