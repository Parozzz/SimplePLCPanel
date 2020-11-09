package parozzz.github.com.hmi.main.others;

import javafx.scene.input.KeyEvent;
import parozzz.github.com.hmi.FXObject;
import parozzz.github.com.hmi.controls.ControlContainerPane;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapper;
import parozzz.github.com.hmi.main.MainEditStage;

import java.util.ArrayList;
import java.util.List;

public class ControlWrapperCopyPasteHandler extends FXObject
{
    private final MainEditStage mainEditStage;
    private final List<ControlWrapper<?>> copySelectionList;

    public ControlWrapperCopyPasteHandler(MainEditStage mainEditStage)
    {
        super("SelectionCopyCutHandler");

        this.mainEditStage = mainEditStage;

        this.copySelectionList = new ArrayList<>();
    }

    @Override
    public void setup()
    {
        super.setup();
        mainEditStage.getStageSetter().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            var controlContainerPane = mainEditStage.getShownControlContainerPane();
            if(keyEvent.isControlDown())
            {
                switch(keyEvent.getCode())
                {
                    case X:
                        this.cutSelectedWrappers(controlContainerPane);
                        break;
                    case C:
                        this.saveSelectedWrappers(controlContainerPane);
                        break;
                    case V:
                        this.pasteSavedWrappers(controlContainerPane);
                        break;
                }
            }
        });
    }

    private void cutSelectedWrappers(ControlContainerPane controlContainerPane)
    {
        this.saveSelectedWrappers(controlContainerPane);
        copySelectionList.forEach(controlContainerPane::deleteControlWrapper);
    }

    private void saveSelectedWrappers(ControlContainerPane controlContainerPane)
    {
        copySelectionList.clear();
        controlContainerPane.getSelectionManager().forEach(copySelectionList::add);
    }

    private void pasteSavedWrappers(ControlContainerPane controlContainerPane)
    {
        for(var controlWrapper : copySelectionList)
        {
            var wrapperType = controlWrapper.getType();

            var layoutX = controlWrapper.getContainerPane().getLayoutX();
            var layoutY = controlWrapper.getContainerPane().getLayoutY();

            var newControlWrapper = controlContainerPane.createControlWrapper(wrapperType);
            newControlWrapper.getContainerPane().relocate(layoutX + 10, layoutY + 10);
            controlWrapper.cloneInto(newControlWrapper);
            newControlWrapper.applyAttributes();
        }

        copySelectionList.clear();
    }
}
