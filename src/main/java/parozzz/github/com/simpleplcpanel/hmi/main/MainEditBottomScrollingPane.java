package parozzz.github.com.simpleplcpanel.hmi.main;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.controls.ControlContainerPane;
import parozzz.github.com.simpleplcpanel.hmi.util.DoubleClickable;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.io.IOException;

public class MainEditBottomScrollingPane
{
    private final HBox hBox;
    public MainEditBottomScrollingPane(HBox hBox)
    {
        this.hBox = hBox;
    }

    public void removeImagePane(ImagePane imagePane)
    {
        hBox.getChildren().remove(imagePane.getAnchorPane());
    }

    public void addImagePane(ImagePane imagePane)
    {
        hBox.getChildren().add(imagePane.getAnchorPane());
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
