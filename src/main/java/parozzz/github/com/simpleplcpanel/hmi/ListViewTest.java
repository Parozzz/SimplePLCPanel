package parozzz.github.com.simpleplcpanel.hmi;

import com.sun.source.tree.Tree;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.ListCellSkin;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

public final class ListViewTest extends HMIStage<StackPane>
{
    private final TreeView<FolderTreeViewItem> treeView;
    public ListViewTest()
    {
        super(new StackPane());

        this.treeView = new TreeView<>();
        init();
    }

    void init()
    {
        treeView.setShowRoot(false);

        treeView.setPadding(new Insets(10));
        treeView.setMinSize(0, 0);
        treeView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        treeView.setCellFactory(tTreeView ->
        {
            var treeCell = new TreeCell<FolderTreeViewItem>()
            {
                @Override
                public void updateItem(FolderTreeViewItem item, boolean empty)
                {
                    super.updateItem(item, empty);

                    if (empty || item == null)
                    {
                        this.setText(null);
                        this.setGraphic(null);
                        return;
                    }

                    if (item instanceof TreeViewItem)
                    {
                        setText(null);

                        var hBox = new HBox();
                        hBox.setMinSize(0, 0);
                        hBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        hBox.setSpacing(10);

                        var label = new Label(item.label);
                        label.setMinSize(0, 0);
                        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        label.setAlignment(Pos.CENTER);

                        hBox.getChildren().addAll(label, ((TreeViewItem) item).node);

                        this.setGraphic(hBox);
                    } else
                    {
                        this.setText(item.label);
                        setGraphic(null);
                    }
                }
            };
            treeCell.setStyle("-fx-indent: 10;");
            treeCell.setMinSize(0, 0);
            treeCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            //treeCell.setBackground(FXUtil.createBackground(Color.WHITE));
            return treeCell;
        });

        var folder1 = new TreeItem<>(new FolderTreeViewItem("folder1"));
        folder1.getChildren().addAll(
                new TreeItem<>(new TreeViewItem("Label1", new TextField())),
                new TreeItem<>(new TreeViewItem("Label1.1", new TextField()))
        );

        var folder2 = new TreeItem<>(new FolderTreeViewItem("folder2"));
        folder2.getChildren().addAll(
                new TreeItem<>(new TreeViewItem("Label2", new CheckBox())),
                new TreeItem<>(new TreeViewItem("Label2.1", new CheckBox()))
        );

        var folder3 = new TreeItem<>(new FolderTreeViewItem("folder3"));

        var folder3_1 = new TreeItem<>(new FolderTreeViewItem("folder3_1"));
        folder3_1.getChildren().addAll(
                new TreeItem<>(new TreeViewItem("Label3_1", new ColorPicker()))
        );

        folder3.getChildren().addAll(
                folder3_1,
                new TreeItem<>(new TreeViewItem("Label3", new ColorPicker())),
                new TreeItem<>(new TreeViewItem("Label3.1", new ColorPicker()))
        );


        var rootItem = new TreeItem<>(new FolderTreeViewItem("root"));
        rootItem.getChildren().addAll(folder1, folder2, folder3);
        treeView.setRoot(rootItem);

        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        super.parent.getChildren().add(treeView);
    }

    private static class TreeViewItem extends FolderTreeViewItem
    {
        private final Node node;

        public TreeViewItem(String label, Node node)
        {
            super(label);

            this.node = node;
        }
    }

    private static class FolderTreeViewItem
    {
        private final String label;

        public FolderTreeViewItem(String label)
        {
            this.label = label;
        }
    }
}
