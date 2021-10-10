package parozzz.github.com.simpleplcpanel.hmi.main.others.copypaste;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.HashMap;

public class ControlWrapperCopyPasteHandler extends FXObject
{
    public static final DataFormat CONTROL_WRAPPER_DATE_FORMAT = new DataFormat("simpleplcpanel/controlwrapper");

    private final MainEditStage mainEditStage;

    public ControlWrapperCopyPasteHandler(MainEditStage mainEditStage)
    {
        this.mainEditStage = mainEditStage;
    }

    @Override
    public void onSetup()
    {
        super.onSetup();
        mainEditStage.getStageSetter().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            var controlContainerPane = mainEditStage.getShownControlContainerPane();

            if (FXUtil.CONTROL_COPY.match(keyEvent))
            {
                this.saveSelectedWrappers(controlContainerPane, false);
            } else if (FXUtil.CONTROL_CUT.match(keyEvent))
            {
                this.saveSelectedWrappers(controlContainerPane, true);
            } else if (FXUtil.CONTROL_PASTE.match(keyEvent))
            {
                this.pasteSavedWrappers(controlContainerPane);
            }
        });
    }

    private void saveSelectedWrappers(ControlContainerPane controlContainerPane, boolean cut)
    {
        var selectionList = new CopyPasteSelectionList();
        controlContainerPane.getMultipleSelectionManager().forEach(controlWrapper ->
        {
            selectionList.add(controlWrapper);
            if(cut)
            {
                controlContainerPane.deleteControlWrapper(controlWrapper);
            }
        });

        var clipboard = Clipboard.getSystemClipboard();

        var clipboardMap = new HashMap<DataFormat, Object>();
        clipboardMap.put(CONTROL_WRAPPER_DATE_FORMAT, selectionList);
        clipboard.setContent(clipboardMap);
    }

    private void pasteSavedWrappers(ControlContainerPane controlContainerPane)
    {
        var clipboard = Clipboard.getSystemClipboard();
        if(!clipboard.hasContent(CONTROL_WRAPPER_DATE_FORMAT))
        {
            return;
        }

        var clipboardContent = clipboard.getContent(CONTROL_WRAPPER_DATE_FORMAT);
        if(!(clipboardContent instanceof CopyPasteSelectionList))
        {
            return;
        }

        var selectionList = (CopyPasteSelectionList) clipboardContent;
        for (var controlWrapper : selectionList.getDeserializedControlWrapperList(controlContainerPane))
        {
            var layoutX = controlWrapper.getLayoutX();
            var layoutY = controlWrapper.getLayoutY();

            while(controlContainerPane.hasControlWrapperNear(layoutX, layoutY, 10, 10))
            {
                layoutX += 10;
                layoutY += 10;
            }

            controlContainerPane.addControlWrapper(controlWrapper);
            controlWrapper.getContainerPane().relocate(layoutX, layoutY);
            controlWrapper.getAttributeUpdater().updateAllAttributes();
        }
    }
}
