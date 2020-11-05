package parozzz.github.com.hmi.controls;

import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.page.HMIStage;

public final class ReadOnlyControlContainerStage extends HMIStage<StackPane>
{
    private final MainEditStage mainEditStage;
    private ControlContainerPane controlContainerPane;
    private boolean stopFullScreen = false;

    public ReadOnlyControlContainerStage(MainEditStage mainEditStage)
    {
        super("ReadOnlyControlMainPage", new StackPane());

        this.mainEditStage = mainEditStage;
    }

    @Override
    public void setup()
    {
        super.setup();

        var stage = super.getStageSetter()
                .initModality(Modality.APPLICATION_MODAL)
                .initStyle(StageStyle.UNDECORATED)
                .setAlwaysOnTop(true)
                .setResizable(true)
                .get();

        //This stop the Stage to be closed in any way
        stage.setOnCloseRequest(windowEvent ->
        {
            if(!stopFullScreen)
            {
                windowEvent.consume();
            }
        });

        stage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if (keyEvent.getCode() == KeyCode.F12 && keyEvent.isControlDown())
            {
                stopFullScreen = !stopFullScreen;
                stage.setFullScreen(!stopFullScreen);
            }
        });

        stage.fullScreenProperty().addListener((observableValue, oldValue, newValue) ->
        {
            if (!stopFullScreen && (newValue == null || !newValue))
            {
                stage.setFullScreen(true);
            }
        });

        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        super.parent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        super.parent.setAlignment(Pos.CENTER);
    }

    public void setExitKeyCombination()
    {
        super.getStageSetter().addEventHandler(KeyEvent.KEY_PRESSED, keyEvent ->
        {
            if (keyEvent.getCode() == KeyCode.ESCAPE)
            {
                super.getStageSetter().close();
                mainEditStage.showStage();

                if (controlContainerPane != null)
                {
                    controlContainerPane.convertToReadWrite();
                    controlContainerPane = null;
                }
            }
        });
    }

    public void setControlMainPage(ControlContainerPane controlContainerPane)
    {
        if (this.controlContainerPane != null)
        {
            this.controlContainerPane.convertToReadWrite();
        }

        this.controlContainerPane = controlContainerPane;

        AnchorPane anchorPane;
        if (controlContainerPane != null)
        {
            controlContainerPane.convertToReadOnly();
            anchorPane = controlContainerPane.getMainAnchorPane();
        } else
        {
            anchorPane = new AnchorPane();
        }

        var children = super.parent.getChildren();
        children.clear();

        StackPane.setAlignment(anchorPane, Pos.CENTER);
        children.add(anchorPane);
    }
}
