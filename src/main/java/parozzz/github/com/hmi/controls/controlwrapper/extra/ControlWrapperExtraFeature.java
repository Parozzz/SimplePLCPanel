package parozzz.github.com.hmi.controls.controlwrapper.extra;

public interface ControlWrapperExtraFeature
{

    public enum Type
    {
        NONE,
        CHANGE_PAGE;
    }

    void bind();

    void unbind();
}
