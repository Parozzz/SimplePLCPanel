package parozzz.github.com.simpleplcpanel.hmi.tags.stage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.FXObject;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIPane;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.function.Consumer;

public final class TagStageSelectionHandler
        extends FXObject
    implements HMIPane
{
    private final TagStage tagStage;

    private final StackPane selectButtonStackPane;
    private final Button selectButton;

    private Consumer<CommunicationTag> selectTagConsumer;

    public TagStageSelectionHandler(TagStage tagStage)
    {
        this.tagStage = tagStage;

        selectButtonStackPane = new StackPane(
                selectButton = new Button()
        );
    }

    @Override
    public void onSetup()
    {
        super.onSetup();

        selectButtonStackPane.setMinSize(0, 0);
        selectButtonStackPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        selectButtonStackPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        selectButtonStackPane.setPadding(new Insets(10));

        StackPane.setAlignment(selectButton, Pos.CENTER);
        selectButton.setText("Select Tag");
        selectButton.setFont(Font.font(null, FontWeight.BOLD,18));
        selectButton.setAlignment(Pos.CENTER);
        selectButton.setMinSize(0, 0);
        selectButton.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        selectButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        selectButton.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
        selectButton.setBackground(FXUtil.createBackground(Color.TRANSPARENT));


        selectButton.setOnAction(event ->
        {
            if(selectTagConsumer == null)
            {
                return;
            }

            var treeItem = tagStage.getTreeTableView().getSelectionModel().getSelectedItem();
            if(treeItem == null)
            {
                return;
            }

            var tag = treeItem.getValue();
            if(tag instanceof CommunicationTag)
            {
                selectTagConsumer.accept((CommunicationTag) tag);
                selectTagConsumer = null;

                tagStage.hideStage();
            }
        });
    }

    @Override
    public Parent getMainParent()
    {
        return selectButtonStackPane;
    }

    void setSelectTagConsumer(Consumer<CommunicationTag> consumer)
    {
        this.selectTagConsumer = consumer;
    }
}
