package parozzz.github.com.hmi.main.dragdrop;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import parozzz.github.com.hmi.FXController;
import parozzz.github.com.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.util.FXUtil;

import java.io.IOException;

public final class DragAndDropPane extends FXController
{
    @FXML private Button createButtonButton;
    @FXML private Button createInputButton;
    @FXML private Button createDisplayButton;

    private final VBox mainVBox;
    private final MainEditStage mainMenuPage;

    public DragAndDropPane(MainEditStage mainMenuPage) throws IOException
    {
        super("DragAndDropPane");

        this.mainVBox = (VBox) FXUtil.loadFXML("dragAndDropMenu.fxml", this);
        this.mainMenuPage = mainMenuPage;

        //#createWrapper needs to be here otherwise it won't call the #setup method
        //for the DraggableControlCreator
        createWrapper("DraggableButtonCreator", createButtonButton, ControlWrapperType.BUTTON);
        createWrapper("DraggableNumericInputCreator", createInputButton, ControlWrapperType.NUMERIC_INPUT);
        createWrapper("DraggableDisplayCreator", createDisplayButton, ControlWrapperType.DISPLAY);
    }

    public VBox getMainVBox()
    {
        return mainVBox;
    }

    private void createWrapper(String name, Button createButton, ControlWrapperType<?, ?> wrapperType)
    {
        var draggableControlCreator = new DraggableControlCreator<>(mainMenuPage, name, createButton, wrapperType);
        this.addFXChild(draggableControlCreator, false);
    }

}
