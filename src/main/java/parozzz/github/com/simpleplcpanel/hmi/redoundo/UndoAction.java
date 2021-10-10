package parozzz.github.com.simpleplcpanel.hmi.redoundo;

@FunctionalInterface
public interface UndoAction
{
    UndoRedoPane undo(boolean returnPaneOnly);
}
