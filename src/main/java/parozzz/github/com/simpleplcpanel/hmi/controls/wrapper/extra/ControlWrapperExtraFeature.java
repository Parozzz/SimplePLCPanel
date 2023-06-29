package parozzz.github.com.simpleplcpanel.hmi.controls.wrapper.extra;

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
