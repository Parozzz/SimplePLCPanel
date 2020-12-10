package parozzz.github.com.hmi.util;

public interface Draggable
{
    void setIsDragged(boolean isDragged);

    void setLastPressedWasDrag(boolean lastPressedWasDrag);

    boolean isDragged();
}
