package parozzz.github.com.simpleplcpanel.hmi.main.dragdrop;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.controlwrapper.ControlWrapperType;
import parozzz.github.com.simpleplcpanel.hmi.main.MainEditStage;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIPane;
import parozzz.github.com.simpleplcpanel.hmi.pane.HidablePane;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class DragAndDropPane
        extends FXController
        implements HMIPane, HidablePane
{
    @FXML private Button createButtonButton;
    @FXML private Button createInputButton;
    @FXML private Button createDisplayButton;

    private final VBox mainVBox;
    private final VBox dragAndDropVBox;
    private final MainEditStage mainMenuPage;

    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    public DragAndDropPane(MainEditStage mainMenuPage) throws IOException
    {
        this.mainVBox = new VBox(
                dragAndDropVBox = (VBox) FXUtil.loadFXML("dragAndDropMenu.fxml", this)
        );

        this.mainMenuPage = mainMenuPage;

        //#createWrapper needs to be here otherwise it won't call the #setup method
        //for the DraggableControlCreator
        createWrapper("DraggableButtonCreator", createButtonButton, ControlWrapperType.BUTTON);
        createWrapper("DraggableNumericInputCreator", createInputButton, ControlWrapperType.NUMERIC_INPUT);
        createWrapper("DraggableDisplayCreator", createDisplayButton, ControlWrapperType.DISPLAY);
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        mainVBox.getChildren().add(0, this.createHideParent(Pos.TOP_LEFT));
        mainVBox.setMinSize(0 ,0);
        mainVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    private void createWrapper(String name, Button createButton, ControlWrapperType<?, ?> wrapperType)
    {
        var draggableControlCreator = new DraggableControlCreator<>(mainMenuPage, name, createButton, wrapperType);
        this.addFXChild(draggableControlCreator, false);
    }

    @Override
    public BooleanProperty visibleProperty()
    {
        return visible;
    }
}
