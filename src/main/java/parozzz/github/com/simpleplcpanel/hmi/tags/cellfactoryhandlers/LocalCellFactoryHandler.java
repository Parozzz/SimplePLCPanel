package parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.skin.CheckBoxSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationDataHolder;
import parozzz.github.com.simpleplcpanel.hmi.tags.CommunicationTag;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public class LocalCellFactoryHandler extends CellFactoryHandler<Boolean>
{
    private final CheckBox checkBox;
    public LocalCellFactoryHandler(CommunicationDataHolder communicationDataHolder)
    {
        super(communicationDataHolder);

        this.checkBox = new CheckBox();
    }

    @Override
    public void init()
    {
        super.init();

        cell.setPadding(Insets.EMPTY);

        checkBox.setMinSize(0, 0);
        checkBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        checkBox.setAlignment(Pos.CENTER);
        checkBox.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        checkBox.setBorder(null);
        checkBox.setText("");

        checkBox.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change ->
        {
            var box = checkBox.lookup(".box");
            if(box instanceof StackPane)
            {
                var boxStackPane = (StackPane) box;
                ((StackPane) box).setBackground(FXUtil.createBackground(Color.TRANSPARENT));
                boxStackPane.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
            }
        });
    }

    @Override
    protected void registerTag(CommunicationTag tag)
    {
        checkBox.setSelected(tag.isLocal());

        tag.localProperty().bindBidirectional(checkBox.selectedProperty());
    }

    @Override
    protected void unregisterTag(CommunicationTag tag)
    {
        tag.localProperty().unbindBidirectional(checkBox.selectedProperty());

        checkBox.setSelected(false);
    }

    @Override
    protected void setGraphic()
    {
        cell.setGraphic(checkBox);
    }
}
