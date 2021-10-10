package parozzz.github.com.simpleplcpanel.hmi.util;

public interface Draggable
{
    boolean canDrag();

    void setIsDragged(boolean isDragged);

    boolean isDragged();

    void setLastPressedWasDrag(boolean lastPressedWasDrag);

    boolean wasLastPressDrag();

}
