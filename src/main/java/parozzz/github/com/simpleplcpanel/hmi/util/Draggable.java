package parozzz.github.com.simpleplcpanel.hmi.util;

public interface Draggable
{
    void setIsDragged(boolean isDragged);

    void setLastPressedWasDrag(boolean lastPressedWasDrag);

    boolean isDragged();
}
