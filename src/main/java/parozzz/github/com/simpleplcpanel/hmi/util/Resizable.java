package parozzz.github.com.simpleplcpanel.hmi.util;

public interface Resizable
{
    boolean canResize();

    void setIsResizing(boolean resizing);

    boolean isResizing();

    boolean wasLastPressResize();

    void setLastPressedWasResize(boolean lastPressedWasResize);
}
