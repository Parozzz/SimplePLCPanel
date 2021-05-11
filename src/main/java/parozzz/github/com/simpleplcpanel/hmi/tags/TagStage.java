package parozzz.github.com.simpleplcpanel.hmi.tags;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationStringAddressData;
import parozzz.github.com.simpleplcpanel.hmi.comm.CommunicationType;
import parozzz.github.com.simpleplcpanel.hmi.pane.HMIStage;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.CommunicationTypeCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.tags.cellfactoryhandlers.StringAddressDataCellFactoryHandler;
import parozzz.github.com.simpleplcpanel.hmi.util.FXUtil;

import java.util.HashSet;
import java.util.Set;

public final class TagStage extends HMIStage<VBox>
{
    private final TreeTableView<Tag> treeTableView;
    private final Tag rootFolderTag;

    private final Set<Tag> tagSet;

    public TagStage()
    {
        super(new VBox());

        this.treeTableView = new TreeTableView<>();
        this.rootFolderTag = new FolderTag("root");

        this.tagSet = new HashSet<>();
    }

    @Override
    public void setup()
    {
        super.setup();

        super.getStageSetter()
                .setResizable(true);

        var topLabel = new Label();
        topLabel.setMinSize(0, 0);
        topLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        topLabel.setText("Tags");
        topLabel.setAlignment(Pos.CENTER);
        topLabel.setBackground(FXUtil.createBackground(Color.GRAY));
        topLabel.setFont(Font.font(null, FontWeight.BOLD, FontPosture.REGULAR, 20));
        topLabel.setTextFill(Color.WHITESMOKE);
        super.parent.getChildren().add(topLabel);

        TreeTableColumn<Tag, String> nameColumn = new TreeTableColumn<>();
        nameColumn.setText("Name");
        nameColumn.setPrefWidth(250);
        nameColumn.setSortable(false);
        nameColumn.setCellValueFactory(features ->
        {
            var treeItem = features.getValue();
            Tag tag;
            if(treeItem == null || (tag = treeItem.getValue()) == null)
            {
                return new SimpleObjectProperty<>();
            }

            return tag.keyValueProperty();
        });
        nameColumn.setCellFactory(tColumn ->
        {
            TreeTableCell<Tag, String> cell = new TextFieldTreeTableCell<>();
            cell.setEditable(true);
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });

        TreeTableColumn<Tag, CommunicationType<?>> typeColumn = new TreeTableColumn<>();
        typeColumn.setText("Type");
        typeColumn.setPrefWidth(125);
        typeColumn.setSortable(false);
        typeColumn.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new CommunicationTypeCellFactoryHandler();
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });

        TreeTableColumn<Tag, CommunicationStringAddressData> addressColumn = new TreeTableColumn<>();
        addressColumn.setText("Address");
        addressColumn.setPrefWidth(170);
        addressColumn.setSortable(false);
        addressColumn.setCellFactory(tColumn ->
        {
            var cellFactoryHandler = new StringAddressDataCellFactoryHandler();
            cellFactoryHandler.init();
            return cellFactoryHandler.getCell();
        });
/*
        treeTableView.setRowFactory(tTableView ->
        {
            var tableRow = new TreeTableRow<Tag>();

            tableRow.setOnKeyPressed(event ->
            {
                var treeItem = tableRow.getTreeItem();
                if(treeItem == null || !event.isAltDown())
                {
                    return;
                }

                var parent = treeItem.getParent();
                switch(event.getCode())
                {
                    case UP:
                        var index = parent.getChildren().indexOf(treeItem);
                        if(index != 0)
                        {
                            parent.getChildren().remove(treeItem);
                            parent.getChildren().add(index, treeItem);
                        }
                        break;
                    case DOWN:

                        break;
                }
            });

            return tableRow;
        }); */

        treeTableView.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        treeTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        treeTableView.setColumnResizePolicy(resizeFeatures -> true);
        treeTableView.setShowRoot(true);
        treeTableView.setRoot(rootFolderTag.createTreeItem());
        treeTableView.getColumns().addAll(nameColumn, typeColumn, addressColumn);
        super.parent.getChildren().add(treeTableView);

        this.addTag(new CommunicationTag("Tag1", CommunicationType.MODBUS_TCP));
        this.addTag(new CommunicationTag("Tag2", CommunicationType.SIEMENS_S7));
        this.addTag(new CommunicationTag("Tag3", CommunicationType.NONE));
        this.addTag(new CommunicationTag("Tag4", CommunicationType.SIEMENS_S7));

        var aFolder = new FolderTag("Folder1!");
        this.addTag(aFolder);

        this.addTag(aFolder, new CommunicationTag("Tag5", CommunicationType.MODBUS_TCP));
        this.addTag(aFolder, new CommunicationTag("Tag6", CommunicationType.SIEMENS_S7));
        this.addTag(aFolder, new CommunicationTag("Tag7", CommunicationType.NONE));
        this.addTag(aFolder, new CommunicationTag("Tag8", CommunicationType.SIEMENS_S7));

        var aSecondFolder = new FolderTag("Folder2!");
        this.addTag(aSecondFolder);

        this.addTag(aSecondFolder, new CommunicationTag("Tag9", CommunicationType.MODBUS_TCP));
        this.addTag(aSecondFolder, new CommunicationTag("Tag10", CommunicationType.SIEMENS_S7));
        this.addTag(aSecondFolder, new CommunicationTag("Tag11", CommunicationType.NONE));
        this.addTag(aSecondFolder, new CommunicationTag("Tag12", CommunicationType.SIEMENS_S7));

    }

    public void addTag(Tag tag)
    {
        this.addTag(null, tag);
    }

    public void addTag(Tag folderTag, Tag tag)
    {
        if(tag.getTreeItem() == null && tagSet.add(tag))
        {
            folderTag = folderTag == null ? rootFolderTag : folderTag;

            var folderTreeItem = folderTag.getTreeItem();
            if(folderTreeItem != null)
            {
                folderTreeItem.getChildren().add(tag.createTreeItem());
            }
        }
    }
}
