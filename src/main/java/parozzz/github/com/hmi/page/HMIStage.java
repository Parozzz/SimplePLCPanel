package parozzz.github.com.hmi.page;

import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.StageStyle;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.redoundo.UndoRedoManager;
import parozzz.github.com.hmi.serialize.JSONSerializable;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public abstract class HMIStage<P extends Parent> extends FXController implements JSONSerializable
{
    private long everyTimestamp;

    protected final P parent;
    private final HMIStageSetter stageSetter;
    private final UndoRedoManager undoRedoManager;

    public HMIStage(String resource, Class<P> parentClass) throws IOException
    {
        this.parent = parentClass.cast(FXUtil.loadFXML(resource, this));
        this.stageSetter = new HMIStageSetter(parent);
        this.undoRedoManager = new UndoRedoManager();
    }


    public HMIStage(String name, String resource, Class<P> parentClass) throws IOException
    {
        super(name);

        this.parent = parentClass.cast(FXUtil.loadFXML(resource, this));
        this.stageSetter = new HMIStageSetter(parent);
        this.undoRedoManager = new UndoRedoManager();
    }

    public HMIStage(P parent)
    {
        this.parent = parent;
        this.stageSetter = new HMIStageSetter(parent);
        this.undoRedoManager = new UndoRedoManager();
    }

    public HMIStage(String name, P parent)
    {
        super(name);

        this.parent = parent;
        this.stageSetter = new HMIStageSetter(parent);
        this.undoRedoManager = new UndoRedoManager();
    }

    @Override
    public void setup()
    {
        super.setup();

        stageSetter.initStyle(StageStyle.DECORATED)
                .setResizable(false);

        var undoKeyCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        var redoKeyCombination = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
        stageSetter.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            switch (keyEvent.getCode())
            {
                case F11:
                    if(stageSetter.isResizable())
                    {
                        stageSetter.setFullScreen(true, "", null);
                    }
                    break;
            }

            if(undoKeyCombination.match(keyEvent))
            {
                keyEvent.consume();
                undoRedoManager.undo();
            }
            else if (redoKeyCombination.match(keyEvent))
            {
                keyEvent.consume();
                undoRedoManager.redo();
            }
        });
    }

    @Override
    public void setDefault()
    {
        super.setDefault();
    }

    public UndoRedoManager getUndoRedoManager()
    {
        return undoRedoManager;
    }

    public WritableImage createSnapshot()
    {
        return parent.snapshot(new SnapshotParameters(), null);
    }

    public HMIStageSetter getStageSetter()
    {
        return stageSetter;
    }

    public void showStage()
    {
        stageSetter.show();
    }

    public boolean every(long millis)
    {
        if (everyTimestamp <= 0 || System.currentTimeMillis() - everyTimestamp > millis)
        {
            everyTimestamp = System.currentTimeMillis();
            return true;
        }

        return false;
    }
}
