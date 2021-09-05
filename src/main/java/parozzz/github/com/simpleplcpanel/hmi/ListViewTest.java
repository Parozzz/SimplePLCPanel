package parozzz.github.com.simpleplcpanel.hmi;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;
import org.controlsfx.control.GridView;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.cell.ImageGridCell;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public final class ListViewTest extends HMIStage<StackPane>
{
    //private final TreeView<FolderTreeViewItem> treeView;
    public ListViewTest()
    {
        super(new StackPane());
        doInit();
        //this.treeView = new TreeView<>();
        //init();
    }

    void doInit()
    {
        var button = new Button();
        super.parent.getChildren().addAll(
                button
        );

        var popOver = new PopOver();
        popOver.setPrefSize(300, 300);
        popOver.setDetachable(false);
        button.setOnMouseClicked(event ->{
            if(!popOver.isShowing())
            {
                popOver.show(button);
            }
        });
        Notifications.create().text("CoolBro").darkStyle().show();
    }
/*
    void init()
    {
        treeView.setStyle("-fx-selection-bar: white; -fx-selection-bar-non-focused: white;");
        treeView.setBackground(null);
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
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (item instanceof TreeViewItem)
                    {
                        //setStyle("-fx-indent: 0");

                        var hBox = new HBox();
                        hBox.setMinSize(0, 0);
                        hBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        hBox.setSpacing(10);

                        var label = new Label(item.label);
                        label.setFont(Font.font(11));
                        label.setMinSize(0, 0);
                        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        label.setAlignment(Pos.CENTER);
                        label.textFillProperty().addListener((observable, oldValue, newValue) ->
                                label.setTextFill(Color.BLACK)
                        );

                        hBox.getChildren().addAll(label, ((TreeViewItem) item).node);

                        setGraphic(hBox);

                        hBox.setBorder(FXUtil.createBorder(Color.LIGHTGRAY, 1));
                    } else
                    {
                        //setStyle("-fx-indent: 10");

                        var label = new Label(item.label);
                        label.setFont(Font.font(11));
                        label.setMinSize(0, 0);
                        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        label.setAlignment(Pos.CENTER_LEFT);
                        label.textFillProperty().addListener((observable, oldValue, newValue) ->
                                label.setTextFill(Color.BLACK)
                        );

                        setGraphic(label);

                        var disclosureNode = getDisclosureNode();
                        if(disclosureNode instanceof Region)
                        {
                            ((Region) disclosureNode).setBorder(
                                    new FXUtil.BorderBuilder()
                                            .left(Color.LIGHTGRAY, 1)
                                            .top(Color.LIGHTGRAY, 1)
                                            .bottom(Color.LIGHTGRAY, 1)
                                            .createBorder()
                            );
                        }

                        label.setBorder(
                                new FXUtil.BorderBuilder()
                                        .top(Color.LIGHTGRAY, 1)
                                        .right(Color.LIGHTGRAY, 1)
                                        .bottom(Color.LIGHTGRAY, 1)
                                        .createBorder()
                        );
                    }
                }
            };
            treeCell.setMinSize(0, 0);
            treeCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            treeCell.setPadding(Insets.EMPTY);
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
        /*treeView.setFocusModel(new FocusModel<>()
        {
            @Override
            protected int getItemCount()
            {
                return 0;
            }

            @Override
            protected TreeItem<FolderTreeViewItem> getModelItem(int index)
            {
                return null;
            }
        });*/
/*
        var scrollPane = new ScrollPane(treeView);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBackground(FXUtil.createBackground(Color.TRANSPARENT));
        super.parent.getChildren().add(scrollPane);
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
    }*/
}
