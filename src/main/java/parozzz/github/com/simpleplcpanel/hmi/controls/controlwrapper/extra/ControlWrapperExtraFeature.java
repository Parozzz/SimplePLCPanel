package parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.extra;

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
