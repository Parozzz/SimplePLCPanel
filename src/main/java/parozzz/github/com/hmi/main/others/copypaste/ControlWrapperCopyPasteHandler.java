package parozzz.github.com.hmi.main.others.copypaste;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.util.FXUtil;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ControlWrapperCopyPasteHandler extends FXObject
{
    public static final DataFormat CONTROL_WRAPPER_DATE_FORMAT = new DataFormat("simpleplcpanel/controlwrapper");

    private final MainEditStage mainEditStage;

    public ControlWrapperCopyPasteHandler(MainEditStage mainEditStage)
    {
        super("SelectionCopyCutHandler");

        this.mainEditStage = mainEditStage;
    }

    @Override
    public void setup()
    {
        super.setup();
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
        controlContainerPane.getSelectionManager().forEach(controlWrapper ->
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
        for (var controlWrapper : selectionList.getControlWrapperList(controlContainerPane))
        {
            var wrapperType = controlWrapper.getType();

            var layoutX = controlWrapper.getLayoutX();
            var layoutY = controlWrapper.getLayoutY();

            while(controlContainerPane.hasControlWrapperNear(layoutX, layoutY, 10, 10))
            {
                layoutX += 10;
                layoutY += 10;
            }

            var newControlWrapper = controlContainerPane.createControlWrapper(wrapperType);
            newControlWrapper.getContainerPane().relocate(layoutX, layoutY);
            controlWrapper.copyInto(newControlWrapper);
            newControlWrapper.getAttributeUpdater().updateAllAttributes();
        }
    }
}
