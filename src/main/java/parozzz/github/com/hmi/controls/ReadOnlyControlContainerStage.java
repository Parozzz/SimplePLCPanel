package parozzz.github.com.hmi.controls;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import parozzz.github.com.hmi.main.MainEditStage;
import parozzz.github.com.hmi.page.HMIStage;

public final class ReadOnlyControlContainerStage extends HMIStage<StackPane>
{
    private final MainEditStage mainEditStage;
    private final Group group;

    private ControlContainerPane controlContainerPane;
    private boolean stopFullScreen = false;

    public ReadOnlyControlContainerStage(MainEditStage mainEditStage)
    {
        super("ReadOnlyControlMainPage", new StackPane());

        this.mainEditStage = mainEditStage;
        super.parent.getChildren().addAll(
                this.group = new Group()
        );
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter().initModality(Modality.APPLICATION_MODAL)
                .initStyle(StageStyle.UNDECORATED)
                //.setFullScreen(true, "", null)
                .setFullScreen(false, "", null)
                .setAlwaysOnTop(true)
                .setResizable(true)
                .addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent ->
                {
                    if (!stopFullScreen)
                    {//This stop the Stage to be closed in any way
                        windowEvent.consume();
                    }
                })
                .addEventHandler(KeyEvent.KEY_PRESSED, keyEvent ->
                {
                    if (keyEvent.getCode() == KeyCode.F12 && keyEvent.isControlDown())
                    {
                        stopFullScreen = !stopFullScreen;
                        super.getStageSetter().setFullScreen(!stopFullScreen, "", null);
                    }
                })
                .get().fullScreenProperty()
                .addListener((observableValue, oldValue, newValue) ->
                {
                    if (!stopFullScreen && (newValue == null || !newValue))
                    {
                        super.getStageSetter().setFullScreen(true, "", null);
                    }
                });

        super.parent.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
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

                if (controlContainerPane != null)
                {
                    mainEditStage.setShownControlContainerPane(controlContainerPane);

                    controlContainerPane.convertToReadWrite();
                    controlContainerPane = null;
                }

                mainEditStage.showStage();
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

        var children = group.getChildren();
        children.clear();

        StackPane.setAlignment(anchorPane, null);
        children.add(anchorPane);

        var scaleX = super.parent.getWidth() / anchorPane.getWidth();
        var scaleY = super.parent.getHeight() / anchorPane.getHeight();

        group.setScaleX(scaleX);
        group.setScaleY(scaleY);
    }
}
