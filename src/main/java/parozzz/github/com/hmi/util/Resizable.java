package parozzz.github.com.hmi.util;

public interface Resizable
{
    boolean canResize();

    void setResizing(boolean resizing);

    void setLastPressedWasResize(boolean lastPressedWasResize);

    boolean isResizing();
}
