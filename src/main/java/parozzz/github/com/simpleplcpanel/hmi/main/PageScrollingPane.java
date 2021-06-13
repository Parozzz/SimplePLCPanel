package parozzz.github.com.simpleplcpanel.hmi.main;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.FXController;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIPane;
import parozzz.github.com.simpleplcpanel.hmi.pane.HidablePane;
import parozzz.github.com.simpleplcpanel.hmi.util.DoubleClickable;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public final class PageScrollingPane
    extends FXController
    implements HMIPane, HidablePane
{
    private final VBox mainVBox;
    private final ScrollPane scrollPane;
    private final HBox pictureHBox;

    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    public PageScrollingPane()
    {
        this.mainVBox = new VBox(
                scrollPane = new ScrollPane(
                        pictureHBox = new HBox()
                )
        );
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        mainVBox.getChildren().add(0, this.createHideParent(Pos.TOP_RIGHT));
        mainVBox.setMinSize(0, 0);
        mainVBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        scrollPane.setBorder(null);

        pictureHBox.setAlignment(Pos.CENTER);
        pictureHBox.setSpacing(4);
        pictureHBox.setFillHeight(true);
    }

    public void removeImagePane(ImagePane imagePane)
    {
        pictureHBox.getChildren().remove(imagePane.getAnchorPane());
    }

    public void addImagePane(ImagePane imagePane)
    {
        pictureHBox.getChildren().add(imagePane.getAnchorPane());
    }

    @Override
    public Parent getMainParent()
    {
        return mainVBox;
    }

    @Override
    public BooleanProperty visibleProperty()
    {
        return visible;
    }

    public static class ImagePane implements DoubleClickable
    {
        @FXML private ImageView pageSnapshotImage;
        @FXML private Label nameLabel;

        private final ControlContainerPane controlContainerPane;
        private final AnchorPane anchorPane;

        public ImagePane(ControlContainerPane controlsPage) throws IOException
        {
            this.controlContainerPane = controlsPage;
            this.anchorPane = (AnchorPane) FXUtil.loadFXML("pageShowingAnchorPane.fxml", this);

            nameLabel.setText(controlsPage.getName());
        }

        public AnchorPane getAnchorPane()
        {
            return anchorPane;
        }

        public void updateSnapshot()
        {
            var anchorPane = controlContainerPane.getMainAnchorPane();

            var params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            var writableImage = anchorPane.snapshot(params, null);
            pageSnapshotImage.setImage(writableImage);
        }

        @Override
        public boolean canDoubleClick()
        {
            return true;
        }
    }
}
